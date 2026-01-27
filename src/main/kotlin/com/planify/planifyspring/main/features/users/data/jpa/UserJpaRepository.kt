package com.planify.planifyspring.main.features.users.data.jpa

import com.planify.planifyspring.main.features.users.data.models.UserModel
import com.planify.planifyspring.main.features.users.domain.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserJpaRepository : JpaRepository<UserModel, Long> {
    fun findByEmail(email: String): UserModel?

    fun findByEmailAndPasswordHash(email: String, passwordHash: String): UserModel?
}
