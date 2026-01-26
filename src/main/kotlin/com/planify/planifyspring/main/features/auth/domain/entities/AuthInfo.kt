package com.planify.planifyspring.main.features.auth.domain.entities

import com.planify.planifyspring.main.features.users.domain.entities.User

data class AuthInfo(
    val user: User,
    val session: AuthSession
)
