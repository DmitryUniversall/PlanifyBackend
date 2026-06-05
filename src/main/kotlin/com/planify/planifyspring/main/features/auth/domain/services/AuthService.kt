package com.planify.planifyspring.main.features.auth.domain.services

import com.planify.planifyspring.main.features.auth.domain.entities.*
import com.planify.planifyspring.main.features.profiles.domain.schemas.CreateProfileSchema
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Locale

interface AuthService {
    fun authenticate(accessToken: String): AuthContext

    fun login(
        email: String,
        passwordRaw: String,
        userAgent: String,
        clientName: String,
        sessionName: String? = null
    ): Pair<AuthContext, AuthTokenPair>

    fun register(
        username: String,
        email: String,
        passwordRaw: String,
        createProfileSchema: CreateProfileSchema,
        userAgent: String,
        clientName: String,
        sessionName: String? = null,
        locale: Locale
    ): String

    fun confirmRegistration(
        confirmationUuid: String,
        code: Int,
        userAgent: String,
        clientName: String,
        sessionName: String? = null
    ): Pair<AuthContext, AuthTokenPair>

    fun resendRegisterConfirmation(confirmationUuid: String, locale: Locale? = null)

    fun refresh(refreshToken: String, currentUserAgent: String): AuthTokenPair

    fun startRecoverPasswordChallenge(email: String, locale: Locale? = null): String
    fun checkRecoverPasswordChallengeCode(challengeUUID: String, code: Int)
    fun recoverPassword(challengeUUID: String, newPassword: String)
    fun getUserActiveRecoverPasswordChallenge(userId: Long): String?

    // fun resendRecoverPasswordChallengeCode(challengeUUID: String, locale: Locale?)  // TODO

    fun getSession(userId: Long, sessionUuid: String): AuthSession
    fun getUserSessions(userId: Long): List<AuthSession>
    fun getActiveUserSessions(userId: Long): List<AuthSession>
    fun revokeSession(userId: Long, sessionUuid: String)

    fun getUserById(id: Long): User
    fun getUserByIdWithAccessInfo(id: Long): Pair<User, AccessInfo>
    fun getAllUsersPaginated(pageable: Pageable): Page<User>

    fun getUserLocaleById(userId: Long): Locale
}
