package com.planify.planifyspring.main.features.users.domain.services_impl

import com.planify.planifyspring.main.config.SecurityConfig
import com.planify.planifyspring.main.exceptions.generics.NotFoundHttpException
import com.planify.planifyspring.main.features.users.domain.entities.User
import com.planify.planifyspring.main.features.users.domain.repositories.UserRepository
import com.planify.planifyspring.main.features.users.domain.services.UsersService
import org.springframework.stereotype.Service

@Service
class UsersServiceImpl(
    private val userRepository: UserRepository
) : UsersService {
    override fun createUser(
        username: String,
        passwordRaw: String,
        email: String
    ): User {
        return userRepository.create(
            username = username,
            email = email,
            passwordHash = SecurityConfig.hashPassword(passwordRaw)
        )
    }

    override fun getUserById(id: Long): User {
        val user = userRepository.getById(id)
        return user ?: throw NotFoundHttpException("User was not found")
    }

    override fun getUserByEmail(email: String): User {
        val user = userRepository.getByEmail(email)
        return user ?: throw NotFoundHttpException("User was not found")
    }

    override fun getUserByAuthCredentials(
        email: String,
        passwordRaw: String
    ): User {
        val user = userRepository.getByAuthCredentials(email, passwordRaw)
        return user ?: throw NotFoundHttpException("User was not found")
    }
}
