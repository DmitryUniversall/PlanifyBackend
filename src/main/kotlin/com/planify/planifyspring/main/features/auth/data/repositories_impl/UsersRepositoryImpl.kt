package com.planify.planifyspring.main.features.auth.data.repositories_impl

import com.planify.planifyspring.core.exceptions.AlreadyExistsAppError
import com.planify.planifyspring.main.common.utils.JsonCacheWrapper
import com.planify.planifyspring.main.common.utils.SecurityHelper
import com.planify.planifyspring.main.features.auth.data.dto.UserCacheDTO
import com.planify.planifyspring.main.features.auth.data.dto.UserWithAccessCacheDTO
import com.planify.planifyspring.main.features.auth.data.jpa.UserJpaRepository
import com.planify.planifyspring.main.features.auth.data.models.UserModel
import com.planify.planifyspring.main.features.auth.domain.entities.AccessInfo
import com.planify.planifyspring.main.features.auth.domain.entities.User
import com.planify.planifyspring.main.features.auth.domain.repositories.UsersRepository
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import tools.jackson.databind.ObjectMapper

@Repository
class UsersRepositoryImpl(
    private val userJpaRepository: UserJpaRepository,
    private val cacheManager: CacheManager,
    private val objectMapper: ObjectMapper
) : UsersRepository {
    private fun usersCache() = JsonCacheWrapper(cacheManager.getCache("users")!!, objectMapper)
    private fun usersWithAccessCache() = JsonCacheWrapper(cacheManager.getCache("usersWithAccess")!!, objectMapper)

    private fun evictUser(id: Long) {
        cacheManager.getCache("users")?.evict(id.toString())
        cacheManager.getCache("usersWithAccess")?.evict(id.toString())
    }

    override fun create(username: String, email: String, passwordHash: String): User {
        val model = UserModel(
            username = username,
            email = email,
            passwordHash = passwordHash
        )

        if (userJpaRepository.existsByEmailOrUsername(email, username)) throw AlreadyExistsAppError("User with this email or username already exists")

        userJpaRepository.save(model)

        val user = model.toEntity()
        usersCache().put(user.id.toString(), UserCacheDTO.fromEntity(user))
        return user
    }

    override fun save(user: User) {
        userJpaRepository.save(UserModel.fromEntity(entity = user))
        evictUser(user.id)
    }

    override fun getById(id: Long): User? {
        val cached = usersCache().getAs<UserCacheDTO>(id.toString())
        if (cached != null) return cached.toEntity()

        return userJpaRepository.findByIdOrNull(id)?.toEntity()
            ?.also { usersCache().put(id.toString(), UserCacheDTO.fromEntity(it)) }
    }

    override fun getByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)?.toEntity()
    }

    override fun getByIdWithAccessInfo(id: Long): Pair<User, AccessInfo>? {
        val cached = usersWithAccessCache().getAs<UserWithAccessCacheDTO>(id.toString())
        if (cached != null) return cached.toEntity()

        val model = userJpaRepository.findByIdWithRolesAndAuthorities(id) ?: return null
        val pair = model.toEntity() to model.getAccessInfo()
        usersWithAccessCache().put(id.toString(), UserWithAccessCacheDTO.fromEntity(pair))
        return pair
    }

    override fun getAllUsersPaginated(pageable: Pageable): Page<User> {
        return userJpaRepository.findAll(pageable).map { it.toEntity() }
    }

    override fun getByAuthCredentials(email: String, passwordRaw: String): User? {
        val model = userJpaRepository.findByEmail(email) ?: return null
        if (!SecurityHelper.isPasswordsMatch(passwordRaw, model.passwordHash)) return null
        return model.toEntity()
    }

    override fun getByAuthCredentialsWithAccessInfo(
        email: String,
        passwordRaw: String
    ): Pair<User, AccessInfo>? {
        val model = userJpaRepository.findByEmailWithRolesAndAuthorities(email) ?: return null
        if (!SecurityHelper.isPasswordsMatch(passwordRaw, model.passwordHash)) return null

        return model.toEntity() to model.getAccessInfo()
    }
}
