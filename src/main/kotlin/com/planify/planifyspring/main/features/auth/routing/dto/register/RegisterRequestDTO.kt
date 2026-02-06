package com.planify.planifyspring.main.features.auth.routing.dto.register

import org.hibernate.validator.constraints.URL
import jakarta.validation.constraints.NotBlank

data class RegisterRequestDTO(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val position: String?,
    val department: String?,

    @field:NotBlank
    @field:URL(
        protocol = "https",
        message = "Profile URL must be a valid HTTPS URL"
    )
    val profileImageUrl: String?
)
