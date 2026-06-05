package com.planify.planifyspring.main.features.auth.domain.entities

import java.time.Instant

data class PasswordRecoveryChallenge(
    val userId: Long,
    val email: String,
    val state: PasswordRecoveryChallengeState = PasswordRecoveryChallengeState.PENDING,
    val attempts: Int = 0,
    val code: Int,
    val uuid: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
