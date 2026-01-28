package com.planify.planifyspring.main.features.auth.domain.entities

import java.io.Serializable
import java.util.*


data class AuthSession(
    val uuid: String,
    val name: String,
    val userId: Long,
    val active: Boolean = true,
    val accessTokenUuid: String,
    val refreshTokenUuid: String,
    val userAgent: String,
    val createdAt: Date,
    val lastUsedAt: Date,
    val expiresAt: Date
) : Serializable
