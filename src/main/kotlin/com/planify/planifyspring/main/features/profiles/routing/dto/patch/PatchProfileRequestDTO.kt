package com.planify.planifyspring.main.features.profiles.routing.dto.patch

data class PatchProfileRequestDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val position: String? = null,
    val department: String? = null,
    val profileImageUrl: String? = null
)
