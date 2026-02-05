package com.planify.planifyspring.main.features.auth.routing.dto

import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext

data class AuthContextDTO(
    val user: UserPrivateDTO,
    val session: AuthSessionPrivateDTO,
    val accessInfo: AccessInfoDTO
) {
    companion object {
        fun fromEntity(entity: AuthContext) = AuthContextDTO(
            user = UserPrivateDTO.fromEntity(entity.user),
            session = AuthSessionPrivateDTO.fromEntity(entity.session),
            accessInfo = AccessInfoDTO.fromEntity(entity.accessInfo)
        )
    }
}
