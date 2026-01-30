package com.planify.planifyspring.main.features.profiles.domain.repositories

import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile
import com.planify.planifyspring.main.features.profiles.domain.schemas.ProfilePatchSchema

interface ProfilesRepository {
    fun getProfileById(userId: Long): Profile?
    fun patchProfile(userId: Long, patch: ProfilePatchSchema)
}
