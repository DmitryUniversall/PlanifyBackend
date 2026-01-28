package com.planify.planifyspring.main.features.profile.domain.services

import com.planify.planifyspring.main.features.profile.domain.entiries.Profile
import com.planify.planifyspring.main.features.profile.domain.utils.ProfilePatchBuilder

interface ProfilesService {
    fun getProfileById(userId: Long): Profile
    fun updateProfile(profile: Profile)
    fun patchProfile(userId: Long, builderFunc: ProfilePatchBuilder.() -> Unit)
}
