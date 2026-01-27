package com.planify.planifyspring.main.features.users.domain.repositories

import com.planify.planifyspring.main.features.users.domain.entities.User

interface UserRepository {
    fun save(user: User)

    fun create(
        username: String,
        email: String,
        passwordHash: String
    ): User

    fun getById(id: Long): User?
    fun getByEmail(email: String): User?
    fun getByAuthCredentials(email: String, passwordRaw: String): User?
}