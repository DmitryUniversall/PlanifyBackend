package com.planify.planifyspring.main.features.auth.domain.entities

import java.io.Serializable

data class AccessInfo(
    val authorities: List<Authority> = emptyList(),
    val roles: List<Role> = emptyList()
) : Serializable  // Serializable for spring security principal
