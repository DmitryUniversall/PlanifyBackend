package com.planify.planifyspring.main.features.profiles.domain.use_cases

import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile
import com.planify.planifyspring.main.features.profiles.domain.schemas.ProfilePatchSchema
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProfilesUseCaseGroup {
    fun getProfileById(userId: Long): Profile

    fun patchProfile(userId: Long, patch: ProfilePatchSchema)

    fun search(input: String, pageable: Pageable): Page<Profile>
}
