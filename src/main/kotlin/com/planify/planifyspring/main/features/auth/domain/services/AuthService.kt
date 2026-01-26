package com.planify.planifyspring.main.features.auth.domain.services

import com.planify.planifyspring.main.features.auth.domain.entities.AuthInfo
import com.planify.planifyspring.main.features.auth.domain.entities.AuthTokenPair

interface AuthService {
    fun authenticate(accessToken: String): AuthInfo

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
    ): Pair<AuthInfo, AuthTokenPair>

    fun register(
        email: String,
        passwordRaw: String,
        userAgent: String,
        sessionName: String
    ): Pair<AuthInfo, AuthTokenPair>
}
