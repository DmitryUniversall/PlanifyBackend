package com.planify.planifyspring.main.features.profiles.domain.services

import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile
import com.planify.planifyspring.main.features.profiles.domain.utils.ProfilePatchBuilder

interface ProfilesService {
    fun getProfileById(userId: Long): Profile
    fun updateProfile(profile: Profile)
    fun patchProfile(userId: Long, builderFunc: ProfilePatchBuilder.() -> Unit)
}
