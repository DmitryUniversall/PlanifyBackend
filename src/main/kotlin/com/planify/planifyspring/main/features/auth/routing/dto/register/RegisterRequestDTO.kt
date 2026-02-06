package com.planify.planifyspring.main.features.auth.routing.dto.register

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

data class RegisterRequestDTO(
    val username: String,
    val email: String,
    val password: String,
    val clientName: String,
    val firstName: String,
    val lastName: String,
    val position: String?,
    val department: String?,

    @field:NotBlank
    @field:URL(
        protocol = "https",
        message = "Profile URL must be a valid HTTPS URL"
    )
    val profileImageUrl: String? = null
)
