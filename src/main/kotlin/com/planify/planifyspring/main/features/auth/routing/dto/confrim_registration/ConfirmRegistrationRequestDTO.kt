package com.planify.planifyspring.main.features.auth.routing.dto.confrim_registration

data class ConfirmRegistrationRequestDTO(
    val code: Int,
    val clientName: String,
    val sessionName: String? = null,
)
