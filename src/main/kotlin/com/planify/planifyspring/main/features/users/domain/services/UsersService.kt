package com.planify.planifyspring.main.features.users.domain.services

import com.planify.planifyspring.main.features.users.domain.entities.User

interface UsersService {
    fun createUser(
        username: String,
        passwordRaw: String,
        email: String
    ): User

    fun getUserById(id: Long): User
    fun getUserByUsername(username: String): User
    fun getUserByAuthCredentials(email: String, passwordRaw: String): User
}
