package com.planify.planifyspring.main.features.auth.domain.entities

data class AuthContext(
    val session: AuthSession,
    val user: User,
    val accessInfo: AccessInfo
)
