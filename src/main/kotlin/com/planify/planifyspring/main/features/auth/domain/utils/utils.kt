package com.planify.planifyspring.main.features.auth.domain.utils

import com.planify.planifyspring.main.features.auth.domain.entities.AuthInfo
import com.planify.planifyspring.main.features.users.domain.entities.User
import org.springframework.web.server.ServerWebExchange


fun ServerWebExchange.authInfo(): AuthInfo? {
    return attributes.getOrDefault("authInfo", null) as AuthInfo?
}
