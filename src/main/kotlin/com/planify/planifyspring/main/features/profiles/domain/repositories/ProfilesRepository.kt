package com.planify.planifyspring.main.features.profiles.domain.repositories

import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile
import com.planify.planifyspring.main.features.profiles.domain.utils.ProfilePatch

interface ProfilesRepository {
    fun getProfileById(userId: Long): Profile?
    fun updateProfile(profile: Profile)
    fun patchProfile(userId: Long, patch: ProfilePatch)
}
