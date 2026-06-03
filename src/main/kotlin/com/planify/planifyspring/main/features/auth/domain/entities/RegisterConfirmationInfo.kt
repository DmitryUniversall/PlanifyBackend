package com.planify.planifyspring.main.features.auth.domain.entities

data class RegisterConfirmationInfo(
    val uuid: String,
    val code: Int,
    val userId: Long,
    val email: String,
)
