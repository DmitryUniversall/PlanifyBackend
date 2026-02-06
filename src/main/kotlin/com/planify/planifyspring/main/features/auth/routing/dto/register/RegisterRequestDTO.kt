package com.planify.planifyspring.main.features.auth.routing.dto.register

data class RegisterRequestDTO(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val position: String?,
    val department: String?,
    val profileImageUrl: String?
)
