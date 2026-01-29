package com.planify.planifyspring.main.features.profiles.data.repositories_impl

import com.planify.planifyspring.main.features.profiles.data.jpa.ProfilesJpaRepository
import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile
import com.planify.planifyspring.main.features.profiles.domain.repositories.ProfilesRepository
import com.planify.planifyspring.main.features.profiles.domain.utils.ProfilePatch
import org.springframework.stereotype.Repository

@Repository
class ProfilesRepositoryImpl(
    private val profilesJpaRepository: ProfilesJpaRepository
) : ProfilesRepository {
    override fun getProfileById(userId: Long): Profile? {
        return profilesJpaRepository.findByUserId(userId)?.toEntity()
    }

    override fun updateProfile(profile: Profile) {
        profilesJpaRepository.parchProfile(
            userId = profile.userId,
            firstName = profile.firstName,
            lastName = profile.lastName,
            position = profile.position,
            department = profile.department,
            profileImageUrl = profile.profileImageUrl,
        )
    }

    override fun patchProfile(
        userId: Long,
        patch: ProfilePatch
    ) {
        profilesJpaRepository.parchProfile(
            userId = userId,
            firstName = patch.firstName,
            lastName = patch.lastName,
            position = patch.position,
            department = patch.department,
            profileImageUrl = patch.profileImageUrl,
        )
    }
}
