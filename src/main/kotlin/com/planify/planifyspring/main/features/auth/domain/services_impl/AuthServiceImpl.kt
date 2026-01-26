package com.planify.planifyspring.main.features.auth.domain.services_impl

import com.planify.planifyspring.main.features.auth.domain.entities.*
import com.planify.planifyspring.main.features.auth.domain.exceptions.InvalidSessionHttpException
import com.planify.planifyspring.main.features.auth.domain.exceptions.SuspiciousActivityDetectedHttpException
import com.planify.planifyspring.main.features.auth.domain.exceptions.TokenExpiredHttpException
import com.planify.planifyspring.main.features.auth.domain.exceptions.TokenInvalidHttpException
import com.planify.planifyspring.main.features.auth.domain.repositories.SessionsRepository
import com.planify.planifyspring.main.features.auth.domain.repositories.TokensRepository
import com.planify.planifyspring.main.features.auth.domain.services.AuthService
import com.planify.planifyspring.main.features.users.domain.entities.User
import com.planify.planifyspring.main.features.users.domain.services.UsersService
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
    private val tokensRepository: TokensRepository,
    private val sessionsRepository: SessionsRepository,
    private val usersService: UsersService
) : AuthService {
    private fun getAccessTokenPayload(accessToken: String): AuthTokenPayload {
        val payload = tokensRepository.decodeJwtToken(accessToken)
        if (payload.type == AuthTokenType.REFRESH) throw TokenInvalidHttpException("Access token expected")
        return payload
    }

    private fun getRefreshTokenPayload(refreshToken: String): AuthTokenPayload {
        val payload = tokensRepository.decodeJwtToken(refreshToken)
        if (payload.type == AuthTokenType.ACCESS) throw TokenInvalidHttpException("Refresh token expected")
        return payload
    }

    private fun isSuspiciousActivity(session: AuthSession, user: User, currentUserAgent: String): Boolean {
        return false
    }

    private fun handleSuspiciousActivity(session: AuthSession, user: User, currentUserAgent: String) {
        // TODO
    }

    private fun getSession(userId: Long, sessionUuid: String): AuthSession {
        return sessionsRepository.getSession(userId = userId, sessionUuid = sessionUuid) ?: throw InvalidSessionHttpException("Unknown session")
    }

    private fun startSession(
        userId: Long,
        userAgent: String,
        sessionName: String
    ): Pair<AuthSession, AuthTokenPair> {

        val newAccessTokenUuid = tokensRepository.generateTokenUuid()
        val newRefreshTokenUuid = tokensRepository.generateTokenUuid()

        val session = sessionsRepository.createSession(
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

    override fun authenticate(accessToken: String): AuthInfo {
        val payload = getAccessTokenPayload(accessToken)

        val session = getSession(userId = payload.userId, sessionUuid = payload.sessionUuid)
        if (session.accessTokenUuid != payload.uuid) throw TokenExpiredHttpException("Invalid token for this session")

        val user = usersService.getUserById(id = payload.userId)
        return AuthInfo(user = user, session = session)
    }

    override fun refresh(refreshToken: String, currentUserAgent: String): AuthTokenPair {
        val payload = getRefreshTokenPayload(refreshToken)

        val session = getSession(userId = payload.userId, sessionUuid = payload.sessionUuid)
        if (session.refreshTokenUuid != payload.uuid) throw TokenExpiredHttpException("Invalid token for this session")

        val user = usersService.getUserById(id = payload.userId)
        if (!isSuspiciousActivity(session, user, currentUserAgent)) {
            handleSuspiciousActivity(session, user, currentUserAgent)
            throw SuspiciousActivityDetectedHttpException(message = "Suspicious activity detected")
        }

        val newAccessTokenPayload = tokensRepository.createAccessTokenPayload(userId = user.id, sessionUuid = session.uuid)
        val newRefreshTokenPayload = tokensRepository.createRefreshTokenPayload(userId = user.id, sessionUuid = session.uuid)

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

    override fun revokeSession(userId: Long, sessionUuid: String) {
        sessionsRepository.revokeSession(userId = userId, sessionUuid = sessionUuid)
    }

    override fun login(
        email: String,
        passwordRaw: String,
        userAgent: String,
        sessionName: String
    ): Pair<AuthInfo, AuthTokenPair> {
        val user = usersService.getUserByAuthCredentials(email, passwordRaw)
        val (session, tokenPair) = startSession(
            userId = user.id,
            userAgent = userAgent,
            sessionName = sessionName
        )

        return AuthInfo(user = user, session = session) to tokenPair
    }

    override fun register(
        email: String,
        passwordRaw: String,
        userAgent: String,
        sessionName: String
    ): Pair<AuthInfo, AuthTokenPair> {
        val user = usersService.getUserByAuthCredentials(email, passwordRaw)
        val (session, tokenPair) = startSession(
            userId = user.id,
            userAgent = userAgent,
            sessionName = sessionName
        )

        return AuthInfo(user = user, session = session) to tokenPair
    }
}
