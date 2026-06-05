package com.planify.planifyspring.main.features.auth.data.dto

import com.planify.planifyspring.main.features.auth.domain.entities.User
import java.io.Serializable
import java.time.Instant
import java.util.Locale

data class UserCacheDTO(
    val id: Long,
    val username: String,
    val email: String,
    val passwordHash: String,
    val activated: Boolean = false,
    val locale: String,
    val lastPasswordRecoveredAt: String
) : Serializable {
    companion object {
        fun fromEntity(entity: User): UserCacheDTO = UserCacheDTO(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            passwordHash = entity.passwordHash,
            activated = entity.isActivated,
            locale = entity.locale.toLanguageTag(),
            lastPasswordRecoveredAt = entity.lastPasswordRecoveredAt.toString()
        )
    }

    fun toEntity(): User = User(
        id = id,
        username = username,
        email = email,
        passwordHash = passwordHash,
        isActivated = activated,
        locale = Locale.forLanguageTag(locale),
        lastPasswordRecoveredAt = Instant.parse(lastPasswordRecoveredAt)
    )
}
