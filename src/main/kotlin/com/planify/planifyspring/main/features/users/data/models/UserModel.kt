package com.planify.planifyspring.main.features.users.data.models

import com.planify.planifyspring.main.features.users.domain.entities.User
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class UserModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val passwordHash: String
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
            id = id,
            username = username,
            email = email,
            passwordHash = passwordHash
        )
    }
}
