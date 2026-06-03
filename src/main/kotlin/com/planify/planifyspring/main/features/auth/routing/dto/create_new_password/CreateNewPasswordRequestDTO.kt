package com.planify.planifyspring.main.features.auth.routing.dto.create_new_password

data class CreateNewPasswordRequestDTO(
    val challengeUUID: String,
    val newPassword: String
)
