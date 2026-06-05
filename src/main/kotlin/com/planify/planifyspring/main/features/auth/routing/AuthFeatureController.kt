package com.planify.planifyspring.main.features.auth.routing

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessApplicationResponse
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.auth.domain.services.AuthService
import com.planify.planifyspring.main.features.auth.routing.dto.*
import com.planify.planifyspring.main.features.auth.routing.dto.confrim_registration.ConfirmRegistrationRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.confrim_registration.ConfirmRegistrationResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.create_new_password.CreateNewPasswordRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.get_auth_context.GetAuthContextResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.get_user_sessions.GetSessionsResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.login.LoginRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.login.LoginResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.password_recovery.PasswordRecoveryRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.password_recovery.PasswordRecoveryResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.refresh.RefreshRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.refresh.RefreshResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.register.RegisterRequestDTO
import com.planify.planifyspring.main.features.auth.routing.dto.register.RegisterResponseDTO
import com.planify.planifyspring.main.features.auth.routing.dto.submit_recover_password_code.SubmitRecoverPasswordCodeRequestDTO
import com.planify.planifyspring.main.features.profiles.domain.schemas.CreateProfileSchema
import jakarta.validation.Valid
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthFeatureController(
    val authService: AuthService,
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
        val confirmationUUID = authService.register(
            email = body.email,
            username = body.username,
            passwordRaw = body.password,
            userAgent = userAgent,
            clientName = body.clientName,
            locale = body.locale?.let { Locale.forLanguageTag(it) } ?: LocaleContextHolder.getLocale(),
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
                confirmationUUID = confirmationUUID,
            ).asSuccessApplicationResponse()
        )
    }

    @PostMapping("/register/{confirmationUuid}/confirm")
    fun confirmRegistration(
        @PathVariable confirmationUuid: String,
        @RequestHeader("User-Agent") userAgent: String,
        @Valid @RequestBody body: ConfirmRegistrationRequestDTO
    ): ResponseEntity<ApplicationResponse<ConfirmRegistrationResponseDTO>> {
        val (info, tokens) = authService.confirmRegistration(
            userAgent = userAgent,
            code = body.code,
            clientName = body.clientName,
            confirmationUuid = confirmationUuid,
        )

        return ResponseEntity.ok(
            ConfirmRegistrationResponseDTO(
                user = UserPrivateDTO.fromEntity(info.user),
                session = AuthSessionPrivateDTO.fromEntity(info.session),
                tokens = AuthTokenPairDTO.fromEntity(tokens),
                accessInfo = AccessInfoDTO.fromEntity(info.accessInfo)
            ).asSuccessApplicationResponse()
        )
    }

    @PostMapping("/register/{confirmationUuid}/resend")
    fun resendRegisterConfirmation(
        @PathVariable confirmationUuid: String
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        authService.resendRegisterConfirmation(confirmationUuid = confirmationUuid)
        return ResponseEntity.ok(ApplicationResponse.success())
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

        sessions.forEach {
            if (it.uuid == authContext.session.uuid) return@forEach
            authService.revokeSession(authContext.user.id, it.uuid)
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

    @PostMapping("/recovery/password")
    fun startRecoverPasswordChallenge(
        @RequestBody body: PasswordRecoveryRequestDTO
    ): ResponseEntity<ApplicationResponse<PasswordRecoveryResponseDTO>> {
        val challengeUUID = authService.startRecoverPasswordChallenge(email = body.email)

        return ResponseEntity.ok(
            PasswordRecoveryResponseDTO(
                challengeUUID = challengeUUID
            ).asSuccessApplicationResponse()
        )
    }

    @PostMapping("/recovery/password/challenge/{challengeUUID}/submit")
    fun submitRecoverPasswordCode(
        @PathVariable challengeUUID: String,
        @RequestBody body: SubmitRecoverPasswordCodeRequestDTO
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        authService.checkRecoverPasswordChallengeCode(challengeUUID = challengeUUID, code = body.code)
        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @PostMapping("/recovery/password/challenge/{challengeUUID}/recover")
    fun createNewPassword(
        @PathVariable challengeUUID: String,
        @RequestBody body: CreateNewPasswordRequestDTO
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        authService.recoverPassword(challengeUUID = challengeUUID, newPassword = body.newPassword)
        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @PostMapping("/recovery/password/challenge{challengeUUID}/resend")
    fun resendRecoverPasswordChallengeCode(
        @PathVariable challengeUUID: String
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        authService.resendRecoverPasswordChallengeCode(challengeUUID = challengeUUID)
        return ResponseEntity.ok(ApplicationResponse.success())
    }
}
