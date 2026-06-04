package com.planify.planifyspring.main.features.fcm.routing

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.fcm.domain.services.FCMTokenService
import com.planify.planifyspring.main.features.fcm.routing.dto.register_device_token.RegisterDeviceTokenRequestDTO
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/fcm/tokens")
class FcmTokenController(
    private val fcmTokenService: FCMTokenService
) {
    @PostMapping("")
    fun registerToken(
        @AuthenticationPrincipal authContext: AuthContext,
        @Valid @RequestBody body: RegisterDeviceTokenRequestDTO,
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        fcmTokenService.registerToken(authContext.user.id, body.token, body.platform)
        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @DeleteMapping("/{token}")
    fun deleteToken(
        @PathVariable token: String,
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        fcmTokenService.deleteToken(token)
        return ResponseEntity.ok(ApplicationResponse.success())
    }
}
