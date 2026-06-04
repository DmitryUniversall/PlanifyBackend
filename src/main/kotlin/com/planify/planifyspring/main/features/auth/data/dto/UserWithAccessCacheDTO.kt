package com.planify.planifyspring.main.features.auth.data.dto

import com.planify.planifyspring.main.features.auth.domain.entities.AccessInfo
import com.planify.planifyspring.main.features.auth.domain.entities.Authority
import com.planify.planifyspring.main.features.auth.domain.entities.Role
import com.planify.planifyspring.main.features.auth.domain.entities.User
import java.io.Serializable

data class RoleCacheDTO(val id: Long, val name: String) : Serializable
data class AuthorityCacheDTO(val id: Long, val name: String) : Serializable

data class UserWithAccessCacheDTO(
    val user: UserCacheDTO,
    val roles: List<RoleCacheDTO>,
    val authorities: List<AuthorityCacheDTO>
) : Serializable {
    companion object {
        fun fromEntity(pair: Pair<User, AccessInfo>): UserWithAccessCacheDTO {
            val (user, access) = pair
            return UserWithAccessCacheDTO(
                user = UserCacheDTO.fromEntity(user),
                roles = access.roles.map { RoleCacheDTO(it.id, it.name) },
                authorities = access.authorities.map { AuthorityCacheDTO(it.id, it.name) }
            )
        }
    }

    fun toEntity(): Pair<User, AccessInfo> = user.toEntity() to AccessInfo(
        roles = roles.map { Role(it.id, it.name) },
        authorities = authorities.map { Authority(it.id, it.name) }
    )
}
