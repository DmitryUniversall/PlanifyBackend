package com.planify.planifyspring.main.features.auth.domain.entities

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val passwordHash: String
)
