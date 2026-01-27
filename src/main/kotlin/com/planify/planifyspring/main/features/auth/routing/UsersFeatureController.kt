package com.planify.planifyspring.main.features.auth.routing

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.routing.auth.ProtectedRoute
import com.planify.planifyspring.main.common.utils.asSuccessResponse
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.auth.routing.dto.UserPrivateDTO
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class UsersFeatureController {
    @ProtectedRoute
    @GetMapping("/me")
    fun getMe(
        @AuthenticationPrincipal authContext: AuthContext
    ): Mono<ApplicationResponse<UserPrivateDTO>> {
        return Mono.just(UserPrivateDTO.fromEntity(entity = authContext.user).asSuccessResponse())
    }
}
