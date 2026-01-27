package com.planify.planifyspring.main.features.auth.domain.entities;

data class AccessInfo(
    val authorities: List<Authority> = emptyList(),
    val roles: List<Role> = emptyList()
)
