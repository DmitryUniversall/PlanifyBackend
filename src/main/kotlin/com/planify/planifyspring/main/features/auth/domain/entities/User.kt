package com.planify.planifyspring.main.features.auth.domain.entities

import java.io.Serializable
import java.time.Instant
import java.util.*

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val passwordHash: String,
    val isActivated: Boolean = false,
    val locale: Locale,
    val lastPasswordRecoveredAt: Instant
) : Serializable
