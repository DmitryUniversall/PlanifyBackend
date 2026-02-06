package com.planify.planifyspring.main.features.auth.domain.use_cases_impl

import com.planify.planifyspring.core.exceptions.AlreadyExistsAppError
import com.planify.planifyspring.core.exceptions.NotFoundAppError
import com.planify.planifyspring.main.exceptions.generics.AlreadyExistsHttpException
import com.planify.planifyspring.main.exceptions.generics.NotFoundHttpException
import com.planify.planifyspring.main.features.auth.domain.entities.*
import com.planify.planifyspring.main.features.auth.domain.exceptions.*
import com.planify.planifyspring.main.features.auth.domain.services.AuthService
import com.planify.planifyspring.main.features.auth.domain.use_cases.AuthUseCaseGroup
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.security.SignatureException

@Component
class AuthUseCaseGroupImpl(
    private val authService: AuthService
) : AuthUseCaseGroup {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private fun decodeJwtToken(token: String): AuthTokenPayload {
        try {
            return authService.decodeJwtToken(token)
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

    private fun isSuspiciousActivity(session: AuthSession, currentUserAgent: String): Boolean {
        return false  // TODO
    }

    private fun handleSuspiciousActivity(session: AuthSession, currentUserAgent: String) {
        // TODO
    }

    override fun getSession(userId: Long, sessionUuid: String): AuthSession {
        val session: AuthSession

        try {
            session = authService.getSession(userId, sessionUuid)
        } catch (_: NotFoundAppError) {
            throw InvalidSessionHttpException("Unknown session")
        }

        if (!session.active) throw InactiveSessionHttpException("This session is no more valid")

        return session
    }

    override fun authenticate(accessToken: String): AuthContext {
        val payload = getAccessTokenPayload(accessToken)

        val session = getSession(userId = payload.userId, sessionUuid = payload.sessionUuid)
        if (session.accessTokenUuid != payload.uuid) throw TokenExpiredHttpException("Invalid token for this session")

        val (user, accessInfo) = getUserByIdWithAccessInfo(id = payload.userId)
        return AuthContext(session = session, user = user, accessInfo = accessInfo)
    }

    override fun revokeSession(userId: Long, sessionUuid: String) {
        authService.revokeSession(userId, sessionUuid)
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

        return authService.rotateSessionTokens(session)
    }

    override fun login(
        email: String,
        passwordRaw: String,
        userAgent: String,
        sessionName: String
    ): Pair<AuthContext, AuthTokenPair> {
        val (user, accessInfo) = getUserByCredentialsWithAccessInfo(email, passwordRaw)

        val (session, tokens) = authService.startSession(
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
        val (session, tokens) = authService.startSession(
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

    override fun createUser(username: String, email: String, passwordRaw: String): User {
        try {
            return authService.createUser(username, email, passwordRaw)
        } catch (error: AlreadyExistsAppError) {
            throw AlreadyExistsHttpException(error.message)
        }
    }

    override fun getUserById(id: Long): User {
        try {
            return authService.getUserById(id)
        } catch (_: NotFoundAppError) {
            throw NotFoundHttpException("User was not found")
        }
    }

    override fun getUserByIdWithAccessInfo(id: Long): Pair<User, AccessInfo> {
        try {
            return authService.getUserByIdWithAccessInfo(id)
        } catch (_: NotFoundAppError) {
            throw NotFoundHttpException("User was not found")
        }
    }

    override fun getAllUsersPaginated(pageable: Pageable): Page<User> {
        return authService.getAllUsersPaginated(pageable)
    }

    override fun getUserByCredentials(email: String, passwordRaw: String): User {
        try {
            return authService.getUserByCredentials(email, passwordRaw)
        } catch (_: NotFoundAppError) {
            throw NotFoundHttpException("User was not found")
        }
    }

    override fun getUserByCredentialsWithAccessInfo(
        email: String,
        passwordRaw: String
    ): Pair<User, AccessInfo> {
        try {
            return authService.getUserByCredentialsWithAccessInfo(email, passwordRaw)
        } catch (_: NotFoundAppError) {
            throw NotFoundHttpException("User was not found")
        }
    }

    override fun getUserSessions(userId: Long): List<AuthSession> {
        return authService.getUserSessions(userId)
    }

    override fun getActiveUserSessions(userId: Long): List<AuthSession> {
        return authService.getActiveUserSessions(userId)
    }
}
