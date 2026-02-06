package com.planify.planifyspring.main.features.auth.domain.entities

import java.io.Serializable
import java.time.Instant


data class AuthSession(
    val uuid: String,
    val name: String,
    val clientName: String,
    val userId: Long,
    val active: Boolean = true,
    val accessTokenUuid: String,
    val refreshTokenUuid: String,
    val userAgent: String,
    val createdAt: Instant,
    val lastUsedAt: Instant,
    val expiresAt: Instant
) : Serializable
