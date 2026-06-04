package com.planify.planifyspring.main.features.fcm.domain.entities

import java.time.Instant

data class FCMToken(
    val token: String,
    val userId: Long,
    val platform: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
