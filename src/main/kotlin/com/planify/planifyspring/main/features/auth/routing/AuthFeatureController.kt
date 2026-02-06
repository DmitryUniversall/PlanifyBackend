package com.planify.planifyspring.main.features.auth.routing

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessApplicationResponse
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.auth.domain.use_cases.AuthUseCaseGroup
import com.planify.planifyspring.main.features.auth.routing.dto.*
import com.planify.planifyspring.main.features.auth.routing.dto.get_auth_context.GetAuthContextResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.get_user_sessions.GetSessionsResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.login.LoginRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.login.LoginResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.refresh.RefreshRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.refresh.RefreshResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.register.RegisterRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.register.RegisterResponseDTO
import com.planify.planifyspring.main.features.profiles.domain.schemas.CreateProfileSchema
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthFeatureController(
    val authUseCaseGroup: AuthUseCaseGroup
) {
    @PostMapping("/login")
    fun login(
        @RequestHeader("User-Agent") userAgent: String,
        @RequestBody body: LoginRequestDTO
    ): ResponseEntity<ApplicationResponse<LoginResponseDTO>> {
        val (info, tokens) = authUseCaseGroup.login(
            email = body.email,
            passwordRaw = body.password,
            userAgent = userAgent,
            clientName = body.clientName
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
        @Valid @RequestBody body: RegisterRequestDTO
    ): ResponseEntity<ApplicationResponse<RegisterResponseDTO>> {
        val (info, tokens) = authUseCaseGroup.register(
            email = body.email,
            username = body.username,
            passwordRaw = body.password,
            userAgent = userAgent,
            clientName = body.clientName,
            createProfileSchema = CreateProfileSchema(
                firstName = body.firstName,
                lastName = body.lastName,
                position = body.position,
                department = body.department,
                profileImageUrl = body.profileImageUrl
            )
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
        val tokens = authUseCaseGroup.refresh(
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
        authUseCaseGroup.revokeSession(userId = authContext.user.id, sessionUuid = authContext.session.uuid)
        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @DeleteMapping("/sessions/{sessionUuid}")
    fun revokeSession(
        @PathVariable sessionUuid: String,
        @AuthenticationPrincipal authContext: AuthContext
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        authUseCaseGroup.revokeSession(userId = authContext.user.id, sessionUuid = sessionUuid)
        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @GetMapping("/sessions/active")
    fun getUserSessions(
        @AuthenticationPrincipal authContext: AuthContext
    ): ResponseEntity<ApplicationResponse<GetSessionsResponseDTO>> {
        val sessions = authUseCaseGroup.getActiveUserSessions(authContext.user.id)

        return ResponseEntity.ok(
            GetSessionsResponseDTO(
                sessions = sessions.map { AuthSessionPrivateDTO.fromEntity(it) }
            ).asSuccessApplicationResponse())
    }

    @DeleteMapping("/sessions/active")
    fun revokeAllSessionsExceptCurrent(
        @AuthenticationPrincipal authContext: AuthContext
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        val sessions = authUseCaseGroup.getActiveUserSessions(authContext.user.id)

        sessions.forEach { // TODO: Optimise it?
            if (it.uuid == authContext.session.uuid) return@forEach
            authUseCaseGroup.revokeSession(authContext.user.id, it.uuid)
        }

        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @GetMapping("/context")
    fun getAuthContext(
        @AuthenticationPrincipal authContext: AuthContext
    ): ResponseEntity<ApplicationResponse<GetAuthContextResponseDTO>> {
        return ResponseEntity.ok(
            GetAuthContextResponseDTO(
                AuthContextDTO.fromEntity(authContext)
            ).asSuccessApplicationResponse()
        )
    }
}
