package com.planify.planifyspring.main.features.auth.domain.events

import java.util.Locale

data class RecoverPasswordEmailRequestedEvent(
    val email: String,
    val code: Int,
    val expiryMinutes: Int,
    val locale: Locale? = null
)
