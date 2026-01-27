package com.planify.planifyspring.main.features.auth.routing.dto.register

data class RegisterRequestDTO(
    val username: String,
    val email: String,
    val password: String
)
