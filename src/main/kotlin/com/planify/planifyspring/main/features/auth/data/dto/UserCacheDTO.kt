package com.planify.planifyspring.main.features.auth.data.dto

import com.planify.planifyspring.main.features.auth.domain.entities.User
import java.io.Serializable

data class UserCacheDTO(
    val id: Long,
    val username: String,
    val email: String,
    val passwordHash: String,
    val activated: Boolean = false
) : Serializable {
    companion object {
        fun fromEntity(entity: User): UserCacheDTO = UserCacheDTO(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            passwordHash = entity.passwordHash,
            activated = entity.isActivated
        )
    }

    fun toEntity(): User = User(
        id = id,
        username = username,
        email = email,
        passwordHash = passwordHash,
        isActivated = activated
    )
}
