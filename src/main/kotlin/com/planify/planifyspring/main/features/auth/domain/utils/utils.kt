package com.planify.planifyspring.main.features.auth.domain.utils

import com.planify.planifyspring.main.features.users.domain.entities.User
import org.springframework.web.server.ServerWebExchange

fun ServerWebExchange.getCurrentUser(): User {
    return User(
        id = 1,
        username = "master",
        email = "master@server.com",
        passwordHash = "aw345hfh23ajk5s6hs543jdf142k432ga65u314312hk4jh4ka45sf"
    )
}
