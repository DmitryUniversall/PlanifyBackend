package com.planify.planifyspring.main.features.profiles.routing.dto.update

data class UpdateProfileRequestDTO(
    val firstName: String,
    val lastName: String,
    val position: String,
    val department: String,
    val profileImageUrl: String
)
