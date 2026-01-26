package com.planify.planifyspring.main.features.auth.domain.entities

import java.util.*

data class AuthTokenPayload(
    val uuid: String,
    val type: AuthTokenType,
    val expiresAt: Date,
    val userId: Long,
    val sessionUuid: String
)
