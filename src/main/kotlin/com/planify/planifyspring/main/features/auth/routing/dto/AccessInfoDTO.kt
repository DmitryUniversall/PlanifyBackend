package com.planify.planifyspring.main.features.auth.routing.dto

import com.planify.planifyspring.main.features.auth.domain.entities.AccessInfo

data class AccessInfoDTO(
    val authorities: List<AuthorityDTO> = emptyList(),
    val roles: List<RoleDTO> = emptyList()
) {
    companion object {
        fun fromEntity(entity: AccessInfo): AccessInfoDTO {
            return AccessInfoDTO(
                authorities = entity.authorities.map { AuthorityDTO.fromEntity(it) },
                roles = entity.roles.map { RoleDTO.fromEntity(it) }
            )
        }
    }
}
