package com.planify.planifyspring.main.features.auth.domain.events

import java.util.Locale

data class ConfirmationEmailRequestedEvent(
    val email: String,
    val code: Int,
    val firstName: String? = null,
    val locale: Locale? = null
)
