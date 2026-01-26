package com.planify.planifyspring.main.features.auth.routing

import com.planify.planifyspring.core.utils.getRandomString
import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessResponse
import com.planify.planifyspring.main.features.auth.domain.entities.AuthInfo
import com.planify.planifyspring.main.features.auth.domain.services.AuthService
import com.planify.planifyspring.main.features.auth.routing.dto.AuthSessionPrivateDTO
import com.planify.planifyspring.main.features.auth.routing.dto.AuthTokenPairDTO
import com.planify.planifyspring.main.features.auth.routing.dto.login.LoginRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.login.LoginResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.refresh.RefreshRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.refresh.RefreshResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.register.RegisterRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.register.RegisterResponseDTO
import com.planify.planifyspring.main.features.users.routing.dto.UserPrivateDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController("/auth")
class AuthFeatureController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun login(
        @RequestHeader("User-Agent") userAgent: String,
        @RequestBody body: LoginRequestDTO
    ): ResponseEntity<ApplicationResponse<LoginResponseDTO>> {
        val (info, tokens) = authService.login(
            email = body.email,
            passwordRaw = body.password,
            userAgent = userAgent,
            sessionName = "${userAgent}-${getRandomString(8)}"
        )

        return ResponseEntity.ok(
            LoginResponseDTO(
                user = UserPrivateDTO.fromUserEntity(info.user),
                session = AuthSessionPrivateDTO.fromEntity(info.session),
                tokens = AuthTokenPairDTO.fromEntity(tokens)
            ).asSuccessResponse()
        )
    }

    @PostMapping("/register")
    fun register(
        @RequestHeader("User-Agent") userAgent: String,
        @RequestBody body: RegisterRequestDTO
    ): ResponseEntity<ApplicationResponse<RegisterResponseDTO>> {
        val (info, tokens) = authService.register(
            email = body.email,
            passwordRaw = body.password,
            userAgent = userAgent,
            sessionName = "${userAgent}-${getRandomString(8)}"
        )

        return ResponseEntity.ok(
            RegisterResponseDTO(
                user = UserPrivateDTO.fromUserEntity(info.user),
                session = AuthSessionPrivateDTO.fromEntity(info.session),
                tokens = AuthTokenPairDTO.fromEntity(tokens)
            ).asSuccessResponse()
        )
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestHeader("User-Agent") userAgent: String,
        @RequestBody body: RefreshRequestDTO
    ): ResponseEntity<ApplicationResponse<RefreshResponseDTO>> {
        val tokens = authService.refresh(
            refreshToken = body.refreshToken,
            currentUserAgent = userAgent
        )

        return ResponseEntity.ok(
            RefreshResponseDTO(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken
            ).asSuccessResponse()
        )
    }

    @GetMapping("/logout")
    fun logout(
        exchange: ServerWebExchange
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        val authInfo = exchange.attributes["authInfo"] as AuthInfo
        authService.revokeSession(userId = authInfo.user.id, sessionUuid = authInfo.session.uuid)
        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @DeleteMapping("/session/{sessionUuid}")
    fun revokeSession(
        @PathVariable sessionUuid: String,
        exchange: ServerWebExchange
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        val authInfo = exchange.attributes["authInfo"] as AuthInfo
        authService.revokeSession(userId = authInfo.user.id, sessionUuid = sessionUuid)
        return ResponseEntity.ok(ApplicationResponse.success())
    }
}
