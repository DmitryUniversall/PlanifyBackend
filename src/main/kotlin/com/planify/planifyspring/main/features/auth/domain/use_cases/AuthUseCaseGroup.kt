package com.planify.planifyspring.main.features.auth.domain.use_cases

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.features.auth.domain.entities.*
import com.planify.planifyspring.main.features.auth.routing.dto.get_all_users.GetAllUsersResponseDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity

interface AuthUseCaseGroup {
    fun authenticate(accessToken: String): AuthContext

    fun getSession(userId: Long, sessionUuid: String): AuthSession
    fun getUserSessions(userId: Long): List<AuthSession>
    fun getActiveUserSessions(userId: Long): List<AuthSession>
    fun revokeSession(userId: Long, sessionUuid: String)

    fun refresh(
        refreshToken: String,
        currentUserAgent: String
    ): AuthTokenPair

    fun login(
        email: String,
        passwordRaw: String,
        userAgent: String,
        sessionName: String
    ): Pair<AuthContext, AuthTokenPair>

    fun register(
        username: String,
        email: String,
        passwordRaw: String,
        userAgent: String,
        sessionName: String
    ): Pair<AuthContext, AuthTokenPair>

    fun createUser(
        username: String,
        email: String,
        passwordRaw: String
    ): User

    fun getUserById(id: Long): User
    fun getUserByIdWithAccessInfo(id: Long): Pair<User, AccessInfo>
    fun getAllUsersPaginated(pageable: Pageable): Page<User>

    fun getUserByCredentials(email: String, passwordRaw: String): User
    fun getUserByCredentialsWithAccessInfo(email: String, passwordRaw: String): Pair<User, AccessInfo>
}
