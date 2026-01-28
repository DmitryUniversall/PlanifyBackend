package com.planify.planifyspring.main.features.profile.domain.utils

class ProfilePatchBuilder {
    var firstName: String? = null
    var lastName: String? = null
    var position: String? = null
    var department: String? = null
    var profileImageUrl: String? = null

    fun build() = ProfilePatch(firstName, lastName, position, department, profileImageUrl)
}

data class ProfilePatch(
    var firstName: String? = null,
    var lastName: String? = null,
    var position: String? = null,
    var department: String? = null,
    var profileImageUrl: String? = null,
)
