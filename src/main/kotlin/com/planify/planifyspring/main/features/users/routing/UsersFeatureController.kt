package com.planify.planifyspring.main.features.users.routing

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.routing.auth.ProtectedRoute
import com.planify.planifyspring.main.common.utils.asSuccessResponse
import com.planify.planifyspring.main.features.auth.domain.utils.getCurrentUser
import com.planify.planifyspring.main.features.users.routing.dto.UserPrivateDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
class UsersFeatureController {
    @ProtectedRoute
    @GetMapping("/me")
    fun getMe(exchange: ServerWebExchange): Mono<ApplicationResponse<UserPrivateDTO>> {
        val user = exchange.getCurrentUser()
        return Mono.just(UserPrivateDTO.fromUserEntity(entity = user).asSuccessResponse())
    }
}
