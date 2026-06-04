package com.planify.planifyspring.main.features.profiles.domain.services_impl

import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile
import com.planify.planifyspring.main.features.profiles.domain.repositories.ProfilesRepository
import com.planify.planifyspring.main.features.profiles.domain.schemas.CreateProfileSchema
import com.planify.planifyspring.main.features.profiles.domain.schemas.PatchProfileSchema
import com.planify.planifyspring.main.features.profiles.domain.services.ProfilesService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfilesServiceImpl(
    private val profilesRepository: ProfilesRepository,
) : ProfilesService {
    override fun getProfileById(userId: Long): Profile? {
        return profilesRepository.getProfileById(userId)
    }

    @Transactional
    override fun patchProfile(userId: Long, patch: PatchProfileSchema) {
        return profilesRepository.patchProfile(userId, patch)
    }

    @Transactional(readOnly = true)
    override fun search(
        input: String,
        pageable: Pageable
    ): Page<Profile> {
        return profilesRepository.search(input, pageable)
    }

    @Transactional
    override fun createProfile(
        userId: Long,
        schema: CreateProfileSchema
    ): Profile {
        return profilesRepository.createProfile(userId, schema)
    }
}
