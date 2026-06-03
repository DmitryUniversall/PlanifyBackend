package com.planify.planifyspring.main.features.auth.routing.dto.submit_recover_password_code

data class SubmitRecoverPasswordCodeRequestDTO(
    val challengeUUID: String,
    val code: Int
)
