package com.planify.planifyspring.main.features.auth.domain.services_impl

import com.planify.planifyspring.core.exceptions.AlreadyExistsAppError
import com.planify.planifyspring.main.common.utils.JsonCacheWrapper
import com.planify.planifyspring.main.common.utils.SecurityHelper
import com.planify.planifyspring.main.exceptions.generics.AlreadyExistsHttpException
import com.planify.planifyspring.main.exceptions.generics.NotFoundHttpException
import com.planify.planifyspring.main.features.auth.domain.entities.*
import com.planify.planifyspring.main.features.auth.domain.exceptions.*
import com.planify.planifyspring.main.features.auth.domain.repositories.SessionsRepository
import com.planify.planifyspring.main.features.auth.domain.repositories.TokensRepository
import com.planify.planifyspring.main.features.auth.domain.repositories.UsersRepository
import com.planify.planifyspring.main.features.auth.domain.services.AuthService
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.security.SignatureException

@Service
class AuthServiceImpl(
    private val tokensRepository: TokensRepository,
    private val sessionsRepository: SessionsRepository,
    private val usersRepository: UsersRepository,
    private val cacheManager: CacheManager,
    private val objectMapper: ObjectMapper
) : AuthService {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private fun isSuspiciousActivity(session: AuthSession, currentUserAgent: String): Boolean {
        return false  // TODO
    }

    private fun handleSuspiciousActivity(session: AuthSession, currentUserAgent: String) {
        // TODO
    }

    private fun generateTokenUuid(): String {
        return tokensRepository.generateTokenUuid()
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

    private fun getSession(userId: Long, sessionUuid: String): AuthSession {
        val cache = JsonCacheWrapper(cacheManager.getCache("sessions")!!, objectMapper)
        val cached = cache.getAs<AuthSession>("$userId-$sessionUuid")
        if (cached != null) return cached

        val session = sessionsRepository.getSession(
            userId = userId,
            sessionUuid = sessionUuid
        ) ?: throw InvalidSessionHttpException("Unknown session")

        if (!session.active) throw InactiveSessionHttpException("This session is no more valid")

        cache.put("$userId-$sessionUuid", session)
        return session
    }

    private fun saveSession(session: AuthSession) {
        val usersCache = cacheManager.getCache("sessions")
        usersCache?.evict("${session.userId}-${session.uuid}")

        sessionsRepository.updateSession(session)
    }

    private fun revokeSession(userId: Long, sessionUuid: String, soft: Boolean = true) {
        val usersCache = cacheManager.getCache("sessions")!!
        usersCache.evict("$userId-$sessionUuid")

        return sessionsRepository.revokeSession(userId = userId, sessionUuid = sessionUuid, soft = soft)
    }

    private fun createSession(
        userId: Long,
        userAgent: String,
        sessionName: String,
        accessTokenUuid: String,
        refreshTokenUuid: String
    ): AuthSession {
        return sessionsRepository.createSession(
            userId = userId,
            userAgent = userAgent,
            sessionName = sessionName,
            accessTokenUuid = accessTokenUuid,
            refreshTokenUuid = refreshTokenUuid,
        ).also {
            val cache = JsonCacheWrapper(cacheManager.getCache("sessions")!!, objectMapper)
            cache.put("${it.userId}-${it.uuid}", it)
        }
    }

    private fun startSession(
        userId: Long,
        userAgent: String,
        sessionName: String
    ): Pair<AuthSession, AuthTokenPair> {

        val newAccessTokenUuid = generateTokenUuid()
        val newRefreshTokenUuid = generateTokenUuid()

        val session = createSession(
            userId = userId,
            userAgent = userAgent,
            sessionName = sessionName,
            accessTokenUuid = newAccessTokenUuid,
            refreshTokenUuid = newRefreshTokenUuid,
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

    override fun authenticate(accessToken: String): AuthContext {
        val payload = getAccessTokenPayload(accessToken)

        val session = getSession(userId = payload.userId, sessionUuid = payload.sessionUuid)
        if (session.accessTokenUuid != payload.uuid) throw TokenExpiredHttpException("Invalid token for this session")

        val (user, accessInfo) = getUserByIdWithAccessInfo(id = payload.userId)
        return AuthContext(session = session, user = user, accessInfo = accessInfo)
    }

    override fun refresh(refreshToken: String, currentUserAgent: String): AuthTokenPair {
        val payload = getRefreshTokenPayload(refreshToken)

        val session = getSession(userId = payload.userId, sessionUuid = payload.sessionUuid)
        if (session.refreshTokenUuid != payload.uuid) throw TokenExpiredHttpException("Invalid token for this session")

        // TODO: Fetch user here to see is it valid and active?

        if (isSuspiciousActivity(session, currentUserAgent)) {
            handleSuspiciousActivity(session, currentUserAgent)
            throw SuspiciousActivityDetectedHttpException(message = "Suspicious activity detected")
        }

        val newAccessTokenPayload = tokensRepository.createAccessTokenPayload(userId = session.userId, sessionUuid = session.uuid)
        val newRefreshTokenPayload = tokensRepository.createRefreshTokenPayload(userId = session.userId, sessionUuid = session.uuid)

        saveSession(
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

    override fun revokeSession(userId: Long, sessionUuid: String) {
        revokeSession(userId = userId, sessionUuid = sessionUuid, soft = true)
    }

    override fun login(
        email: String,
        passwordRaw: String,
        userAgent: String,
        sessionName: String
    ): Pair<AuthContext, AuthTokenPair> {
        val (user, accessInfo) = getUserByCredentialsWithAccessInfo(email, passwordRaw)

        val (session, tokens) = startSession(
            userId = user.id,
            userAgent = userAgent,
            sessionName = sessionName
        )

        return AuthContext(
            session = session,
            user = user,
            accessInfo = accessInfo
        ) to tokens
    }

    override fun register(
        username: String,
        email: String,
        passwordRaw: String,
        userAgent: String,
        sessionName: String
    ): Pair<AuthContext, AuthTokenPair> {
        val user = createUser(username, email, passwordRaw)
        val (session, tokens) = startSession(
            userId = user.id,
            userAgent = userAgent,
            sessionName = sessionName
        )

        return AuthContext(
            session = session,
            user = user,
            accessInfo = AccessInfo()
        ) to tokens
    }

    override fun createUser(
        username: String,
        email: String,
        passwordRaw: String
    ): User {
        try {
            return usersRepository.create(
                username = username,
                email = email,
                passwordHash = SecurityHelper.hashPassword(passwordRaw)
            ).also {
                val cache = JsonCacheWrapper(cacheManager.getCache("users")!!, objectMapper)
                cache.put(it.id.toString(), it)
            }
        } catch (error: AlreadyExistsAppError) {
            throw AlreadyExistsHttpException(error.message)
        }
    }

    override fun getUserById(id: Long): User {
        val cache = JsonCacheWrapper(cacheManager.getCache("users")!!, objectMapper)
        val cached = cache.getAs<User>(id.toString())
        if (cached != null) return cached

        val user = usersRepository.getById(id)
        return user ?: throw NotFoundHttpException("User was not found")
    }

    override fun getUserByIdWithAccessInfo(id: Long): Pair<User, AccessInfo> {
        val cache = JsonCacheWrapper(cacheManager.getCache("usersWithAccess")!!, objectMapper)
        val cached = cache.getAs<Pair<User, AccessInfo>>(id.toString())
        if (cached != null) return cached

        val result = usersRepository.getByIdWithAccessInfo(id)
        return result ?: throw NotFoundHttpException("User was not found")
    }

    override fun getUserByCredentials(  // TODO: Cache?
        email: String,
        passwordRaw: String
    ): User {
        val user = usersRepository.getByAuthCredentials(email, passwordRaw)
        return user ?: throw NotFoundHttpException("User was not found")
    }

    override fun getUserByCredentialsWithAccessInfo(email: String, passwordRaw: String): Pair<User, AccessInfo> {
        val result = usersRepository.getByAuthCredentialsWithAccessInfo(email, passwordRaw)
        return result ?: throw NotFoundHttpException("User was not found")
    }

    override fun getUserSessions(userId: Long): List<AuthSession> {
        return sessionsRepository.getUserSessions(userId)
    }

    override fun getActiveUserSessions(userId: Long): List<AuthSession> {
        return sessionsRepository.getActiveUserSessions(userId)
    }
}
