package com.planify.planifyspring.main.features.auth.routing

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessApplicationResponse
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.auth.routing.dto.UserPrivateDTO
import com.planify.planifyspring.main.features.auth.routing.dto.me.GetMeResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UsersFeatureController {
    @GetMapping("/me")
    fun getMe(
        @AuthenticationPrincipal authContext: AuthContext
    ): ResponseEntity<ApplicationResponse<GetMeResponseDTO>> {
        return ResponseEntity.ok(
            GetMeResponseDTO(
                user = UserPrivateDTO.fromEntity(entity = authContext.user)
            ).asSuccessApplicationResponse()
        )
    }
}
