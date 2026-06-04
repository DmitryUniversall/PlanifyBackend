package com.planify.planifyspring.main.features.auth.data.dto

import com.planify.planifyspring.main.features.auth.domain.entities.AuthSession
import java.io.Serializable
import java.time.Instant

data class AuthSessionDTO(
    val uuid: String,
    val name: String,
    val clientName: String,
    val userId: Long,
    val active: Boolean = true,
    val accessTokenUuid: String,
    val refreshTokenUuid: String,
    val userAgent: String,
    val createdAt: Instant,
    val lastUsedAt: Instant,
    val expiresAt: Instant
) : Serializable {
    companion object {
        fun fromEntity(entity: AuthSession): AuthSessionDTO = AuthSessionDTO(
            uuid = entity.uuid,
            name = entity.name,
            clientName = entity.clientName,
            userId = entity.userId,
            active = entity.active,
            accessTokenUuid = entity.accessTokenUuid,
            refreshTokenUuid = entity.refreshTokenUuid,
            userAgent = entity.userAgent,
            createdAt = entity.createdAt,
            lastUsedAt = entity.lastUsedAt,
            expiresAt = entity.expiresAt
        )
    }

    fun toEntity(): AuthSession = AuthSession(
        uuid = uuid,
        name = name,
        clientName = clientName,
        userId = userId,
        active = active,
        accessTokenUuid = accessTokenUuid,
        refreshTokenUuid = refreshTokenUuid,
        userAgent = userAgent,
        createdAt = createdAt,
        lastUsedAt = lastUsedAt,
        expiresAt = expiresAt
    )
}
