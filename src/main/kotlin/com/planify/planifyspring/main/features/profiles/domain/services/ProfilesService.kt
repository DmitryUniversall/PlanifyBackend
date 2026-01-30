package com.planify.planifyspring.main.features.profiles.domain.services

import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile
import com.planify.planifyspring.main.features.profiles.domain.schemas.ProfilePatchSchema

interface ProfilesService {
    fun getProfileById(userId: Long): Profile
    fun patchProfile(userId: Long, patch: ProfilePatchSchema)
}
