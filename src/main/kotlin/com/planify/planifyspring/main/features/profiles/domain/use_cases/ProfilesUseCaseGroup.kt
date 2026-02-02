package com.planify.planifyspring.main.features.profiles.domain.use_cases

import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile
import com.planify.planifyspring.main.features.profiles.domain.schemas.ProfilePatchSchema

interface ProfilesUseCaseGroup {
    fun getProfileById(userId: Long): Profile

    fun patchProfile(userId: Long, patch: ProfilePatchSchema)
}
