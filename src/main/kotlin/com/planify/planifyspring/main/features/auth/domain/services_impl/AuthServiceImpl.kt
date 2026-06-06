package com.planify.planifyspring.main.features.auth.domain.services_impl

import com.planify.planifyspring.core.exceptions.NotFoundAppError
import com.planify.planifyspring.core.exceptions.TooManyRequestsAppError
import com.planify.planifyspring.core.utils.getRandomString
import com.planify.planifyspring.core.utils.within
import com.planify.planifyspring.main.common.utils.SecurityHelper
import com.planify.planifyspring.main.exceptions.generics.WrongCredentialsHttpException
import com.planify.planifyspring.main.features.auth.domain.entities.*
import com.planify.planifyspring.main.features.auth.domain.events.ConfirmationEmailRequestedEvent
import com.planify.planifyspring.main.features.auth.domain.events.RecoverPasswordEmailRequestedEvent
import com.planify.planifyspring.main.features.auth.domain.exceptions.*
import com.planify.planifyspring.main.features.auth.domain.repositories.AuthEmailRepository
import com.planify.planifyspring.main.features.auth.domain.repositories.SessionsRepository
import com.planify.planifyspring.main.features.auth.domain.repositories.TokensRepository
import com.planify.planifyspring.main.features.auth.domain.repositories.UsersRepository
import com.planify.planifyspring.main.features.auth.domain.services.AuthService
import com.planify.planifyspring.main.features.profiles.domain.schemas.CreateProfileSchema
import com.planify.planifyspring.main.features.profiles.domain.services.ProfilesService
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SignatureException
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.time.Duration.Companion.minutes

@Service
class AuthServiceImpl(
    private val tokensRepository: TokensRepository,
    private val sessionsRepository: SessionsRepository,
    private val usersRepository: UsersRepository,
    private val authEmailRepository: AuthEmailRepository,
    private val profilesService: ProfilesService,
    private val eventPublisher: ApplicationEventPublisher,
) : AuthService {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val EMAIL_RESEND_TIMEOUT_MINUTES = 1L

        private const val REGISTRATION_CONFIRMATION_TTL_MINUTES = 10L
        private val REGISTRATION_CONFIRMATION_TTL: Duration = Duration.ofMinutes(REGISTRATION_CONFIRMATION_TTL_MINUTES)

        private const val PASSWORD_RECOVERY_TTL_MINUTES = 10L
        private const val PASSWORD_RECOVERY_TIMEOUT_MINUTES = 0L // 30L  // FIXME: Temp disabled
        private val PASSWORD_RECOVERY_TTL: Duration = Duration.ofMinutes(PASSWORD_RECOVERY_TTL_MINUTES)
        private const val MAX_RECOVERY_ATTEMPTS = 5
    }

    private fun generateTokenUuid(): String = tokensRepository.generateTokenUuid()

    private fun generateConfirmationUUID(): String = UUID.randomUUID().toString()

    private fun generateConfirmationCode(): Int = (100000..999999).random()

    private fun generateDefaultSessionName(clientName: String, userAgent: String): String {
        return "${clientName}-${userAgent}-${getRandomString(8)}"
    }

    private fun decodeJwtToken(token: String): AuthTokenPayload {
        try {
            return tokensRepository.decodeJwtToken(token)
        } catch (_: UnsupportedJwtException) {
            throw TokenInvalidHttpException("Token uses an unsupported jwt algorithm")
        } catch (_: MalformedJwtException) {
            throw TokenInvalidHttpException("Token structure is invalid")
        } catch (_: SignatureException) {
            throw TokenInvalidHttpException("Signature validation failed")
        } catch (_: ExpiredJwtException) {
            throw TokenExpiredHttpException("Token expired")
        } catch (error: JwtException) {
            logger.warn("Unknown token validation error: ${error::class.qualifiedName}: ${error.message}")
            throw TokenExpiredHttpException("Unknown token validation error")
        }
    }

    private fun getAccessTokenPayload(accessToken: String): AuthTokenPayload {
        val payload = decodeJwtToken(accessToken)
        if (payload.type == AuthTokenType.REFRESH) throw TokenInvalidHttpException("Access token expected")
        return payload
    }

    private fun getRefreshTokenPayload(refreshToken: String): AuthTokenPayload {
        val payload = decodeJwtToken(refreshToken)
        if (payload.type == AuthTokenType.ACCESS) throw TokenInvalidHttpException("Refresh token expected")
        return payload
    }

    private fun getActiveSession(userId: Long, sessionUuid: String): AuthSession {
        val session = sessionsRepository.getSession(userId = userId, sessionUuid = sessionUuid)
            ?: throw InvalidSessionHttpException("Unknown session")
        if (!session.active) throw InactiveSessionHttpException("This session is no more valid")
        return session
    }

    @Suppress("unused")
    private fun isSuspiciousActivity(session: AuthSession, currentUserAgent: String): Boolean {
        return false
    }

    @Suppress("unused")
    private fun handleSuspiciousActivity(session: AuthSession, currentUserAgent: String) {
    }

    private fun startSession(
        userId: Long,
        userAgent: String,
        sessionName: String,
        clientName: String
    ): Pair<AuthSession, AuthTokenPair> {
        val newAccessTokenUuid = generateTokenUuid()
        val newRefreshTokenUuid = generateTokenUuid()

        val session = sessionsRepository.createSession(
            userId = userId,
            userAgent = userAgent,
            sessionName = sessionName,
            accessTokenUuid = newAccessTokenUuid,
            refreshTokenUuid = newRefreshTokenUuid,
            clientName = clientName
        )

        return session to AuthTokenPair(
            accessToken = tokensRepository.createAccessToken(
                userId = userId,
                sessionUuid = session.uuid,
                tokenUuid = newAccessTokenUuid
            ),
            refreshToken = tokensRepository.createRefreshToken(
                userId = userId,
                sessionUuid = session.uuid,
                tokenUuid = newRefreshTokenUuid
            )
        )
    }

    private fun rotateSessionTokens(session: AuthSession): AuthTokenPair {
        val newAccessTokenPayload = tokensRepository.createAccessTokenPayload(userId = session.userId, sessionUuid = session.uuid)
        val newRefreshTokenPayload = tokensRepository.createRefreshTokenPayload(userId = session.userId, sessionUuid = session.uuid)

        sessionsRepository.updateSession(
            session.copy(
                accessTokenUuid = newAccessTokenPayload.uuid,
                refreshTokenUuid = newRefreshTokenPayload.uuid,
            )
        )

        return AuthTokenPair(
            accessToken = tokensRepository.createAccessToken(newAccessTokenPayload),
            refreshToken = tokensRepository.createRefreshToken(newRefreshTokenPayload),
        )
    }

    private fun createUser(
        username: String,
        email: String,
        passwordRaw: String,
        locale: Locale,
        createProfileSchema: CreateProfileSchema
    ): User {
        val user = usersRepository.create(
            username = username,
            email = email,
            passwordHash = SecurityHelper.hashPassword(passwordRaw),
            locale = locale
        )

        profilesService.createProfile(user.id, createProfileSchema)

        return user
    }

    private fun activateUser(user: User): User {
        val activated = user.copy(isActivated = true)
        usersRepository.save(activated)
        return activated
    }

    private fun updateUserPassword(user: User, newPasswordRaw: String): User {
        val updated = user.copy(passwordHash = SecurityHelper.hashPassword(newPasswordRaw))
        usersRepository.save(updated)
        return updated
    }

    private fun getUserByEmail(email: String): User {
        return usersRepository.getByEmail(email) ?: throw NotFoundAppError("User was not found")
    }

    private fun getUserByCredentialsWithAccessInfo(email: String, passwordRaw: String): Pair<User, AccessInfo> {
        return usersRepository.getByAuthCredentialsWithAccessInfo(email, passwordRaw)
            ?: throw NotFoundAppError("User was not found")
    }

    private fun generateRegisterConfirmationInfo(userId: Long, email: String): RegisterConfirmationInfo {
        return RegisterConfirmationInfo(
            uuid = generateConfirmationUUID(),
            code = generateConfirmationCode(),
            userId = userId,
            email = email,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun getRegisterConfirmationInfoOrThrow(uuid: String): RegisterConfirmationInfo {
        return authEmailRepository.getRegisterConfirmationInfo(uuid)
            ?: throw ExpiredRegisterConfirmationCodeHttpException()
    }

    private fun publishConfirmationEmail(email: String, code: Int, firstName: String?, locale: Locale?) {
        eventPublisher.publishEvent(
            ConfirmationEmailRequestedEvent(
                email = email,
                code = code,
                firstName = firstName,
                expiryMinutes = REGISTRATION_CONFIRMATION_TTL_MINUTES.toInt(),
                locale = locale
            )
        )
    }

    private fun generateRecoverPasswordChallenge(userId: Long, email: String): PasswordRecoveryChallenge {
        return PasswordRecoveryChallenge(
            userId = userId,
            email = email,
            code = generateConfirmationCode(),
            uuid = generateConfirmationUUID(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun getRecoverPasswordChallengeOrThrow(challengeUUID: String): PasswordRecoveryChallenge {
        return authEmailRepository.getRecoverPasswordChallenge(challengeUUID)
            ?: throw NotFoundAppError("Recovery challenge was not found")
    }

    private fun saveRecoverPasswordChallenge(challenge: PasswordRecoveryChallenge) {
        authEmailRepository.saveRecoverPasswordChallenge(challenge, PASSWORD_RECOVERY_TTL)
    }

    private fun publishRecoverPasswordEmail(email: String, code: Int, locale: Locale?) {
        eventPublisher.publishEvent(
            RecoverPasswordEmailRequestedEvent(
                email = email,
                code = code,
                expiryMinutes = PASSWORD_RECOVERY_TTL_MINUTES.toInt(),
                locale = locale
            )
        )
    }

    override fun authenticate(accessToken: String): AuthContext {
        val payload = getAccessTokenPayload(accessToken)

        val session = getActiveSession(userId = payload.userId, sessionUuid = payload.sessionUuid)
        if (session.accessTokenUuid != payload.uuid) throw TokenExpiredHttpException("Invalid token for this session")

        val (user, accessInfo) = try {
            getUserByIdWithAccessInfo(payload.userId)
        } catch (_: NotFoundAppError) {
            throw UnknownUserHttpException("User of this session no longer exists")
        }

        return AuthContext(session = session, user = user, accessInfo = accessInfo)
    }

    override fun login(
        email: String,
        passwordRaw: String,
        userAgent: String,
        clientName: String,
        sessionName: String?
    ): Pair<AuthContext, AuthTokenPair> {
        val (user, accessInfo) = try {
            getUserByCredentialsWithAccessInfo(email, passwordRaw)
        } catch (_: NotFoundAppError) {
            throw WrongCredentialsHttpException("Wrong email or password")
        }

        val (session, tokens) = startSession(
            userId = user.id,
            userAgent = userAgent,
            sessionName = sessionName ?: generateDefaultSessionName(clientName, userAgent),
            clientName = clientName
        )

        return AuthContext(session = session, user = user, accessInfo = accessInfo) to tokens
    }

    @Transactional
    override fun register(
        username: String,
        email: String,
        passwordRaw: String,
        createProfileSchema: CreateProfileSchema,
        userAgent: String,
        clientName: String,
        sessionName: String?,
        locale: Locale
    ): String {
        val user = createUser(username, email, passwordRaw, locale, createProfileSchema)

        val info = generateRegisterConfirmationInfo(userId = user.id, email = email)
        authEmailRepository.saveRegisterConfirmationInfo(info, REGISTRATION_CONFIRMATION_TTL)

        publishConfirmationEmail(email = email, code = info.code, firstName = createProfileSchema.firstName, locale = locale)

        return info.uuid
    }

    @Transactional
    override fun confirmRegistration(
        confirmationUuid: String,
        code: Int,
        userAgent: String,
        clientName: String,
        sessionName: String?
    ): Pair<AuthContext, AuthTokenPair> {
        val confirmationInfo = getRegisterConfirmationInfoOrThrow(confirmationUuid)
        if (code != confirmationInfo.code) throw InvalidRegisterConfirmationCodeHttpException()

        val userInactive = getUserById(confirmationInfo.userId)
        val userActivated = activateUser(userInactive)

        val (session, tokens) = startSession(
            userId = userActivated.id,
            userAgent = userAgent,
            sessionName = sessionName ?: generateDefaultSessionName(clientName, userAgent),
            clientName = clientName
        )

        return AuthContext(session = session, user = userActivated, accessInfo = AccessInfo()) to tokens
    }

    override fun resendRegisterConfirmation(confirmationUuid: String, locale: Locale?) {
        val info = getRegisterConfirmationInfoOrThrow(confirmationUuid)

        if (info.updatedAt within EMAIL_RESEND_TIMEOUT_MINUTES.minutes) throw TooManyRequestsAppError("Unable to resend email: too many requests")

        val messageLocale = locale ?: getUserLocaleById(info.userId)

        val updatedInfo = info.copy(code = generateConfirmationCode(), updatedAt = Instant.now())
        authEmailRepository.saveRegisterConfirmationInfo(updatedInfo, REGISTRATION_CONFIRMATION_TTL)
        publishConfirmationEmail(email = updatedInfo.email, code = updatedInfo.code, firstName = null, locale = messageLocale)
    }

    override fun refresh(refreshToken: String, currentUserAgent: String): AuthTokenPair {
        val payload = getRefreshTokenPayload(refreshToken)

        val session = getActiveSession(userId = payload.userId, sessionUuid = payload.sessionUuid)
        if (session.refreshTokenUuid != payload.uuid) throw TokenExpiredHttpException("Invalid token for this session")

        if (isSuspiciousActivity(session, currentUserAgent)) {
            handleSuspiciousActivity(session, currentUserAgent)
            throw SuspiciousActivityDetectedHttpException(message = "Suspicious activity detected")
        }

        return rotateSessionTokens(session)
    }

    @Transactional
    override fun startRecoverPasswordChallenge(email: String, locale: Locale?): String {
        val user = getUserByEmail(email)

        if (user.lastPasswordRecoveredAt within PASSWORD_RECOVERY_TIMEOUT_MINUTES.minutes) throw PasswordRecoveryRateLimitHttpException()

        // TODO: Security issue? (recover lock)
        if (getUserActiveRecoverPasswordChallenge(user.id) != null) throw PasswordRecoveryInProcessHttpException()

        val messageLocale = locale ?: user.locale

        val challenge = generateRecoverPasswordChallenge(userId = user.id, email = email)

        saveRecoverPasswordChallenge(challenge)
        publishRecoverPasswordEmail(email = challenge.email, code = challenge.code, locale = messageLocale)

        return challenge.uuid
    }

    override fun checkRecoverPasswordChallengeCode(challengeUUID: String, code: Int) {
        val challenge = getRecoverPasswordChallengeOrThrow(challengeUUID)

        if (challenge.state == PasswordRecoveryChallengeState.FAILED) throw RecoverPasswordChallengeFailedHttpException()
        if (challenge.state == PasswordRecoveryChallengeState.PASSED) throw RecoverPasswordChallengeAlreadyPassedHttpException()
        if (challenge.state != PasswordRecoveryChallengeState.PENDING) throw BadRecoverPasswordChallengeStateHttpException()

        if (challenge.code != code) {
            saveRecoverPasswordChallenge(
                challenge.copy(
                    attempts = challenge.attempts + 1,
                    state = if (challenge.attempts <= MAX_RECOVERY_ATTEMPTS) PasswordRecoveryChallengeState.PENDING else PasswordRecoveryChallengeState.FAILED
                )
            )

            throw RecoverPasswordChallengeAttemptFailedHttpException()
        }

        saveRecoverPasswordChallenge(challenge.copy(state = PasswordRecoveryChallengeState.PASSED))
    }

    @Transactional
    override fun recoverPassword(challengeUUID: String, newPassword: String) {
        val challenge = getRecoverPasswordChallengeOrThrow(challengeUUID)

        if (challenge.state == PasswordRecoveryChallengeState.FAILED) throw RecoverPasswordChallengeFailedHttpException()
        if (challenge.state != PasswordRecoveryChallengeState.PASSED) throw RecoverPasswordChallengeNotPassedHttpException()

        val user = getUserById(challenge.userId)
        updateUserPassword(user, newPassword)
        authEmailRepository.deleteRecoverPasswordChallenge(challengeUUID, user.id)
        // TODO: Send action to notify user
    }

    override fun getUserActiveRecoverPasswordChallenge(userId: Long): String? {
        return authEmailRepository.getUserActiveRecoverPasswordChallengeUUID(userId)
    }

    override fun resendRecoverPasswordChallengeCode(challengeUUID: String, locale: Locale?) {
        val challenge = getRecoverPasswordChallengeOrThrow(challengeUUID)

        if (challenge.state == PasswordRecoveryChallengeState.FAILED) throw RecoverPasswordChallengeFailedHttpException()
        if (challenge.state == PasswordRecoveryChallengeState.PASSED) throw RecoverPasswordChallengeAlreadyPassedHttpException()
        if (challenge.state != PasswordRecoveryChallengeState.PENDING) throw BadRecoverPasswordChallengeStateHttpException()

        if (challenge.updatedAt within EMAIL_RESEND_TIMEOUT_MINUTES.minutes) throw TooManyRequestsAppError("Unable to resend email: too many requests")

        val messageLocale = locale ?: getUserLocaleById(challenge.userId)
        val updated = challenge.copy(code = generateConfirmationCode(), updatedAt = Instant.now())

        saveRecoverPasswordChallenge(updated)

        publishRecoverPasswordEmail(email = challenge.email, code = challenge.code, locale = messageLocale)
    }

    override fun getSession(userId: Long, sessionUuid: String): AuthSession {
        return sessionsRepository.getSession(userId = userId, sessionUuid = sessionUuid)
            ?: throw NotFoundAppError("Session not found")
    }

    override fun getUserSessions(userId: Long): List<AuthSession> {
        return sessionsRepository.getUserSessions(userId)
    }

    override fun getActiveUserSessions(userId: Long): List<AuthSession> {
        return sessionsRepository.getActiveUserSessions(userId)
    }

    override fun revokeSession(userId: Long, sessionUuid: String) {
        return sessionsRepository.revokeSession(userId = userId, sessionUuid = sessionUuid, soft = true)
    }

    override fun getUserById(id: Long): User {
        return usersRepository.getById(id) ?: throw NotFoundAppError("User was not found")
    }

    override fun getUserByIdWithAccessInfo(id: Long): Pair<User, AccessInfo> {
        return usersRepository.getByIdWithAccessInfo(id) ?: throw NotFoundAppError("User was not found")
    }

    override fun getAllUsersPaginated(pageable: Pageable): Page<User> {
        return usersRepository.getAllUsersPaginated(pageable)
    }

    override fun getUserLocaleById(userId: Long): Locale {
        return getUserById(userId).locale  // Users are cached, so this operation is still fast
    }
}
