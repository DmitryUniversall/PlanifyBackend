package com.planify.planifyspring.main.features.auth.domain.services

import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.auth.domain.entities.AuthTokenPair
import com.planify.planifyspring.main.features.auth.domain.entities.User
import com.planify.planifyspring.main.features.auth.domain.entities.AccessInfo
import com.planify.planifyspring.main.features.auth.domain.entities.AuthSession

interface AuthService {
    fun authenticate(accessToken: String): AuthContext

    fun refresh(refreshToken: String, currentUserAgent: String): AuthTokenPair

    fun revokeSession(
        userId: Long,
        sessionUuid: String
    )

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

    fun getUserByCredentials(email: String, passwordRaw: String): User
    fun getUserByCredentialsWithAccessInfo(email: String, passwordRaw: String): Pair<User, AccessInfo>

    fun getUserSessions(userId: Long): List<AuthSession>
    fun getActiveUserSessions(userId: Long): List<AuthSession>
}
