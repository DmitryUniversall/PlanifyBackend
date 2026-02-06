package com.planify.planifyspring.main.features.auth.data.repositories_impl

import com.planify.planifyspring.main.common.utils.SecurityHelper
import com.planify.planifyspring.main.common.utils.redis.RedisHelper
import com.planify.planifyspring.main.features.auth.domain.entities.AuthSession
import com.planify.planifyspring.main.features.auth.domain.repositories.SessionsRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class SessionsRepositoryImpl(
    private val helper: RedisHelper
) : SessionsRepository {
    private fun generateSessionUuid(): String {
        return UUID.randomUUID().toString()
    }

    private fun getUserSessionsKey(userId: Long): String {
        return "auth:user:$userId:sessions"
    }

    private fun getUserSessionKey(userId: Long, sessionUuid: String): String {
        return "auth:user:$userId:sessions:$sessionUuid"
    }

    private fun writeSession(session: AuthSession) {
        helper.hset(
            key = getUserSessionKey(userId = session.userId, sessionUuid = session.uuid),
            value = session
        )
    }

    override fun createSession(
        userId: Long,
        userAgent: String,
        sessionName: String,
        clientName: String,
        accessTokenUuid: String,
        refreshTokenUuid: String,
    ): AuthSession {
        val session = AuthSession(
            uuid = generateSessionUuid(),
            name = sessionName,
            userId = userId,
            accessTokenUuid = accessTokenUuid,
            refreshTokenUuid = refreshTokenUuid,
            userAgent = userAgent,
            createdAt = Instant.now(),
            lastUsedAt = Instant.now(),
            expiresAt = SecurityHelper.calculateSessionExpiresAt(),
            clientName = clientName
        )

        writeSession(session)

        return session
    }

    override fun getSession(
        userId: Long, sessionUuid: String
    ): AuthSession? {
        return helper.hget(
            key = getUserSessionKey(userId, sessionUuid),
            clazz = AuthSession::class.java
        )
    }

    override fun getUserSessions(userId: Long): List<AuthSession> {
        return helper.hgetAllSubkeys(
            base = "${getUserSessionsKey(userId)}:*",
            clazz = AuthSession::class.java
        )
    }

    override fun getActiveUserSessions(userId: Long): List<AuthSession> {
        return getUserSessions(userId).filter { it.active }
    }

    override fun revokeSession(userId: Long, sessionUuid: String, soft: Boolean) {
        val sessionKey = getUserSessionKey(userId, sessionUuid)
        if (soft) {
            updateSession(userId = userId, sessionUuid = sessionUuid, set = "active" to false)
        } else {
            helper.hdel(key = sessionKey)
        }
    }

    override fun <T : Any> updateSession(userId: Long, sessionUuid: String, set: Pair<String, T>) {
        helper.hsetField(
            key = getUserSessionKey(userId, sessionUuid),
            field = set.first,
            value = set.second
        )
    }

    override fun updateSession(updatedSession: AuthSession) {
        writeSession(updatedSession)
    }
}
