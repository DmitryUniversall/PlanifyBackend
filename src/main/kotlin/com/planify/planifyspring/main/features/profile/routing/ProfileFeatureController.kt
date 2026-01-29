package com.planify.planifyspring.main.features.profile.routing

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessResponse
import com.planify.planifyspring.main.features.profile.domain.services.ProfilesService
import com.planify.planifyspring.main.features.profile.routing.dto.ProfileDTO
import com.planify.planifyspring.main.features.profile.routing.dto.get_profile.GetProfileResponseDTO
import com.planify.planifyspring.main.features.profile.routing.dto.patch.PatchProfileRequestDTO
import com.planify.planifyspring.main.features.profile.routing.dto.update.UpdateProfileRequestDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users/{userId}/profile")
class ProfileFeatureController(
    private val profilesService: ProfilesService
) {
    @GetMapping("")
    fun getProfile(
        @PathVariable userId: Long
    ): ResponseEntity<ApplicationResponse<GetProfileResponseDTO>> {
        val profile = profilesService.getProfileById(userId)

        return ResponseEntity.ok(
            GetProfileResponseDTO(
                profile = ProfileDTO.fromEntity(profile)
            ).asSuccessResponse()
        )
    }

    @PatchMapping("")
    fun patchProfile(
        @PathVariable userId: Long,
        @RequestBody body: PatchProfileRequestDTO
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        profilesService.patchProfile(userId) {
            firstName = body.firstName
            lastName = body.lastName
            position = body.position
            department = body.department
            profileImageUrl = body.profileImageUrl
        }

        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @PutMapping("")
    fun updateProfile(
        @PathVariable userId: Long,
        @RequestBody body: UpdateProfileRequestDTO
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        profilesService.patchProfile(userId) {
            firstName = body.firstName
            lastName = body.lastName
            position = body.position
            department = body.department
            profileImageUrl = body.profileImageUrl
        }

        return ResponseEntity.ok(ApplicationResponse.success())
    }
}
