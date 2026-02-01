package com.planify.planifyspring.main.features.auth.data.models

import com.planify.planifyspring.main.features.auth.domain.entities.AccessInfo
import com.planify.planifyspring.main.features.auth.domain.entities.User
import jakarta.persistence.*

@Entity
@Table(name = "users")
open class UserModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val passwordHash: String,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val roles: MutableSet<RoleModel> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_authorities",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "authority_id")]
    )
    val authorities: MutableSet<AuthorityModel> = mutableSetOf(),
) {
    companion object {
        fun fromEntity(entity: User): UserModel {
            return UserModel(
                id = entity.id,
                username = entity.username,
                email = entity.email,
                passwordHash = entity.passwordHash
            )
        }
    }

    fun toEntity(): User {
        return User(
            id = id!!,
            username = username,
            email = email,
            passwordHash = passwordHash
        )
    }

    fun getAccessInfo(): AccessInfo {
        val userRoles = roles.map { it.toEntity() }
        val userAuthorities = (authorities.map { it.toEntity() } + userRoles.flatMap { role -> authorities.map { it.toEntity() } }).distinct().toList()

        return AccessInfo(
            roles = userRoles,
            authorities = userAuthorities
        )
    }
}
