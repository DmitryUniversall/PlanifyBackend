package com.planify.planifyspring.main.features.users.domain.services_impl;

import com.planify.planifyspring.main.features.users.domain.entities.User
import com.planify.planifyspring.main.features.users.domain.services.UsersService
import org.springframework.stereotype.Service

@Service
class UsersServiceImpl : UsersService {
    override fun createUser(
        username: String,
        passwordRaw: String,
        email: String
    ): User {
        TODO("Not yet implemented")
    }

    override fun getUserById(id: Long): User {
        TODO("Not yet implemented")
    }

    override fun getUserByUsername(username: String): User {
        TODO("Not yet implemented")
    }

    override fun getUserByAuthCredentials(
        email: String,
        passwordRaw: String
    ): User {
        TODO("Not yet implemented")
    }

}
