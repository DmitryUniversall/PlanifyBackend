package com.planify.planifyspring.main.features.auth.routing.dto

import com.planify.planifyspring.main.features.auth.domain.entities.User

interface UserPrivateDTOI : UserPublicDTOI {
    val email: String
}

data class UserPrivateDTO(
    override val id: Long,
    override val username: String,
    override val email: String
) : UserPrivateDTOI {
    companion object {
        fun fromEntity(entity: User): UserPrivateDTO = UserPrivateDTO(
            id = entity.id,
            username = entity.username,
            email = entity.email
        )
    }
}
