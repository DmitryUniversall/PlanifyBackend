package com.planify.planifyspring.main.features.profiles.domain.schemas

data class CreateProfileSchema(
    var firstName: String,
    var lastName: String,
    var position: String? = null,
    var department: String? = null,
    var profileImageUrl: String? = null
)
