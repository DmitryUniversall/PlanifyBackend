package com.planify.planifyspring.main.features.users.data.repositories

import com.planify.planifyspring.main.config.SecurityConfig
import com.planify.planifyspring.main.features.users.data.jpa.UserJpaRepository
import com.planify.planifyspring.main.features.users.data.models.UserModel
import com.planify.planifyspring.main.features.users.domain.entities.User
import com.planify.planifyspring.main.features.users.domain.repositories.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository
) : UserRepository {
    override fun create(username: String, email: String, passwordHash: String): User {
        val model = UserModel(
            username = username,
            email = email,
            passwordHash = passwordHash
        )

        jpaRepository.save(model)

        return model.toEntity()
    }

    override fun save(user: User) {
        jpaRepository.save(UserModel.fromEntity(entity = user))
    }

    override fun getById(id: Long): User? {
        return jpaRepository.findByIdOrNull(id)?.toEntity()
    }

    override fun getByEmail(email: String): User? {
        return jpaRepository.findByEmail(email)?.toEntity()
    }

    override fun getByAuthCredentials(email: String, passwordRaw: String): User? {
        val passwordHash = SecurityConfig.hashPassword(passwordRaw)
        return jpaRepository.findByEmailAndPasswordHash(email, passwordHash)?.toEntity()
    }
}
