package com.planify.planifyspring.main.features.auth.routing.dto

import com.planify.planifyspring.main.features.auth.domain.entities.AuthSession
import java.time.Instant

data class AuthSessionPrivateDTO(
    val uuid: String,
    val name: String,
    val userId: Long,
    val clientName: String,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val lastUsedAt: Instant,
    val expiresAt: Instant
) {
    companion object {
        fun fromEntity(entity: AuthSession): AuthSessionPrivateDTO {
            return AuthSessionPrivateDTO(
                uuid = entity.uuid,
                name = entity.name,
                userId = entity.userId,
                isActive = entity.active,
                createdAt = entity.createdAt,
                lastUsedAt = entity.lastUsedAt,
                expiresAt = entity.expiresAt,
                clientName = entity.clientName
            )
        }
    }
}
