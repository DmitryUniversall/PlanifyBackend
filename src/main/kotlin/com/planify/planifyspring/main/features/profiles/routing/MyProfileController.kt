package com.planify.planifyspring.main.features.profiles.routing

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessApplicationResponse
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.profiles.domain.schemas.ProfilePatchSchema
import com.planify.planifyspring.main.features.profiles.domain.services.ProfilesService
import com.planify.planifyspring.main.features.profiles.routing.dto.ProfileDTO
import com.planify.planifyspring.main.features.profiles.routing.dto.get_profile.GetProfileResponseDTO
import com.planify.planifyspring.main.features.profiles.routing.dto.patch.PatchProfileRequestDTO
import com.planify.planifyspring.main.features.profiles.routing.dto.update.UpdateProfileRequestDTO
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/profiles/my")
class MyProfileController(
    private val profilesService: ProfilesService
) {
    @GetMapping("")
    fun getProfile(
        @AuthenticationPrincipal authContext: AuthContext
    ): ResponseEntity<ApplicationResponse<GetProfileResponseDTO>> {
        val profile = profilesService.getProfileById(authContext.user.id)

        return ResponseEntity.ok(
            GetProfileResponseDTO(
                profile = ProfileDTO.fromEntity(profile)
            ).asSuccessApplicationResponse()
        )
    }

    @PatchMapping("")
    fun patchProfile(
        @AuthenticationPrincipal authContext: AuthContext,
        @RequestBody body: PatchProfileRequestDTO
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        profilesService.patchProfile(authContext.user.id, ProfilePatchSchema(
            firstName = body.firstName,
            lastName = body.lastName,
            position = body.position,
            department = body.department,
            profileImageUrl = body.profileImageUrl
        ))

        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @PutMapping("")
    fun updateProfile(
        @AuthenticationPrincipal authContext: AuthContext,
        @RequestBody body: UpdateProfileRequestDTO
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        profilesService.patchProfile(authContext.user.id, ProfilePatchSchema(
            firstName = body.firstName,
            lastName = body.lastName,
            position = body.position,
            department = body.department,
            profileImageUrl = body.profileImageUrl
        ))

        return ResponseEntity.ok(ApplicationResponse.success())
    }
}
