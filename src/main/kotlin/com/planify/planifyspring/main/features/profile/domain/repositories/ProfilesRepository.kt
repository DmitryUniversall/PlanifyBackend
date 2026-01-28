package com.planify.planifyspring.main.features.profile.domain.repositories

import com.planify.planifyspring.main.features.profile.domain.entiries.Profile
import com.planify.planifyspring.main.features.profile.domain.utils.ProfilePatch

interface ProfilesRepository {
    fun getProfileById(userId: Long): Profile?
    fun updateProfile(profile: Profile)
    fun patchProfile(userId: Long, patch: ProfilePatch)
}
