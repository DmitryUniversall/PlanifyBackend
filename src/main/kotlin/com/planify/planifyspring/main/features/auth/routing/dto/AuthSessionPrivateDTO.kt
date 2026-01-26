package com.planify.planifyspring.main.features.auth.routing.dto

import com.planify.planifyspring.main.features.auth.domain.entities.AuthSession
import java.util.*

data class AuthSessionPrivateDTO(
    val uuid: String,
    val name: String,
    val userId: Long,
    val isActive: Boolean = true,
    val createdAt: Date,
    val lastUsedAt: Date,
    val expiresAt: Date
) {
    companion object {
        fun fromEntity(entity: AuthSession): AuthSessionPrivateDTO {
            return AuthSessionPrivateDTO(
                uuid = entity.uuid,
                name = entity.name,
                userId = entity.userId,
                isActive = entity.isActive,
                createdAt = entity.createdAt,
                lastUsedAt = entity.lastUsedAt,
                expiresAt = entity.expiresAt
            )
        }
    }
}
