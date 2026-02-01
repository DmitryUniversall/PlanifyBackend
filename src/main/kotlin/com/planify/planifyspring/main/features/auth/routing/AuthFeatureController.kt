package com.planify.planifyspring.main.features.auth.routing

import com.planify.planifyspring.core.utils.getRandomString
import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessApplicationResponse
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.auth.domain.services.AuthService
import com.planify.planifyspring.main.features.auth.routing.dto.AccessInfoDTO
import com.planify.planifyspring.main.features.auth.routing.dto.AuthSessionPrivateDTO
import com.planify.planifyspring.main.features.auth.routing.dto.AuthTokenPairDTO
import com.planify.planifyspring.main.features.auth.routing.dto.UserPrivateDTO
import com.planify.planifyspring.main.features.auth.routing.dto.get_user_sessions.GetSessionsResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.login.LoginRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.login.LoginResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.refresh.RefreshRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.refresh.RefreshResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.register.RegisterRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.register.RegisterResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
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
                user = UserPrivateDTO.fromEntity(info.user),
                session = AuthSessionPrivateDTO.fromEntity(info.session),
                tokens = AuthTokenPairDTO.fromEntity(tokens),
                accessInfo = AccessInfoDTO.fromEntity(info.accessInfo)
            ).asSuccessApplicationResponse()
        )
    }

    @PostMapping("/register")
    fun register(
        @RequestHeader("User-Agent") userAgent: String,
        @RequestBody body: RegisterRequestDTO
    ): ResponseEntity<ApplicationResponse<RegisterResponseDTO>> {
        val (info, tokens) = authService.register(
            email = body.email,
            username = body.username,
            passwordRaw = body.password,
            userAgent = userAgent,
            sessionName = "${userAgent}-${getRandomString(8)}"
        )

        return ResponseEntity.ok(
            RegisterResponseDTO(
                user = UserPrivateDTO.fromEntity(info.user),
                session = AuthSessionPrivateDTO.fromEntity(info.session),
                tokens = AuthTokenPairDTO.fromEntity(tokens),
                accessInfo = AccessInfoDTO.fromEntity(info.accessInfo)
            ).asSuccessApplicationResponse()
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
            ).asSuccessApplicationResponse()
        )
    }

    @DeleteMapping("/logout")
    fun logout(
        @AuthenticationPrincipal authContext: AuthContext
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        authService.revokeSession(userId = authContext.user.id, sessionUuid = authContext.session.uuid)
        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @DeleteMapping("/sessions/{sessionUuid}")
    fun revokeSession(
        @PathVariable sessionUuid: String,
        @AuthenticationPrincipal authContext: AuthContext
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        authService.revokeSession(userId = authContext.user.id, sessionUuid = sessionUuid)
        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @GetMapping("/sessions/active")
    fun getUserSessions(
        @AuthenticationPrincipal authContext: AuthContext
    ): ResponseEntity<ApplicationResponse<GetSessionsResponseDTO>> {
        val sessions = authService.getActiveUserSessions(authContext.user.id)

        return ResponseEntity.ok(
            GetSessionsResponseDTO(
            sessions = sessions.map { AuthSessionPrivateDTO.fromEntity(it) }
        ).asSuccessApplicationResponse())
    }

    @DeleteMapping("/sessions/active")
    fun revokeAllSessionsExceptCurrent(
        @AuthenticationPrincipal authContext: AuthContext
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        val sessions = authService.getActiveUserSessions(authContext.user.id)

        sessions.forEach { // TODO: Optimise it?
            if (it.uuid == authContext.session.uuid) return@forEach
            authService.revokeSession(authContext.user.id, it.uuid)
        }

        return ResponseEntity.ok(ApplicationResponse.success())
    }
}
