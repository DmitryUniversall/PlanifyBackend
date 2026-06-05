package com.planify.planifyspring.main.features.auth.domain.entities

import java.time.Instant


data class RegisterConfirmationInfo(
    val uuid: String,
    val code: Int,
    val userId: Long,
    val email: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
