package com.planify.planifyspring.main.features.auth.data.repositories_impl

import com.planify.planifyspring.main.common.redis.RedisJsonHelper
import com.planify.planifyspring.main.common.utils.SecurityHelper
import com.planify.planifyspring.main.features.auth.domain.entities.AuthSession
import com.planify.planifyspring.main.features.auth.domain.repositories.SessionsRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class SessionsRepositoryImpl(
    private val helper: RedisJsonHelper
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
        helper.hsetDataClass(
            key = getUserSessionKey(userId = session.userId, sessionUuid = session.uuid),
            value = session
        )
    }

    override fun createSession(
        userId: Long,
        userAgent: String,
        sessionName: String,
        accessTokenUuid: String,
        refreshTokenUuid: String
    ): AuthSession {
        val session = AuthSession(
            uuid = generateSessionUuid(),
            name = sessionName,
            userId = userId,
            accessTokenUuid = accessTokenUuid,
            refreshTokenUuid = refreshTokenUuid,
            userAgent = userAgent,
            createdAt = Date(),
            lastUsedAt = Date(),
            expiresAt = SecurityHelper.calculateSessionExpiresAt()
        )

        writeSession(session)

        return session
    }

    override fun getSession(
        userId: Long, sessionUuid: String
    ): AuthSession? {
        return helper.hgetDataClass(
            key = getUserSessionKey(userId, sessionUuid),
            clazz = AuthSession::class.java
        )
    }

    override fun getUserSessions(userId: Long): List<AuthSession> {
        return helper.hgetAllDataClasses(
            base = getUserSessionsKey(userId),
            clazz = AuthSession::class.java
        )
    }

    override fun revokeSession(userId: Long, sessionUuid: String, soft: Boolean) {
        val sessionKey = getUserSessionKey(userId, sessionUuid)
        if (soft) {
            updateSession(userId = userId, sessionUuid = sessionUuid, set = "active" to false)
        } else {
            helper.hdel(key = sessionKey)
        }
    }

    override fun <T> updateSession(userId: Long, sessionUuid: String, set: Pair<String, T>) {
        helper.hset(
            key = getUserSessionKey(userId, sessionUuid),
            field = set.first,
            value = set.second
        )
    }

    override fun updateSession(updatedSession: AuthSession) {
        writeSession(updatedSession)
    }
}
