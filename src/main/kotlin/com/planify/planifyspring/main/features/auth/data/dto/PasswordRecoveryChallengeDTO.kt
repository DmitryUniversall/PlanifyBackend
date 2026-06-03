package com.planify.planifyspring.main.features.auth.data.dto

import com.planify.planifyspring.main.features.auth.domain.entities.PasswordRecoveryChallenge
import com.planify.planifyspring.main.features.auth.domain.entities.PasswordRecoveryChallengeState

data class PasswordRecoveryChallengeDTO(
    val userId: Long,
    val email: String,
    val state: Int,
    val attempts: Int,
    val code: Int,
    val uuid: String
) {
    companion object {
        fun fromEntity(entity: PasswordRecoveryChallenge) = PasswordRecoveryChallengeDTO(
            userId = entity.userId,
            email = entity.email,
            state = entity.state.ordinal,
            attempts = entity.attempts,
            code = entity.code,
            uuid = entity.uuid
        )
    }

    fun toEntity(): PasswordRecoveryChallenge = PasswordRecoveryChallenge(
        userId = userId,
        email = email,
        state = PasswordRecoveryChallengeState.entries[state],
        attempts = attempts,
        code = code,
        uuid = uuid
    )
}
