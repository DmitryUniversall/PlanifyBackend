package com.planify.planifyspring.main.features.profiles.data.repositories_impl

import com.planify.planifyspring.main.common.utils.JsonCacheWrapper
import com.planify.planifyspring.main.features.profiles.data.dto.ProfileCacheDTO
import com.planify.planifyspring.main.features.profiles.data.jpa.ProfilesJpaRepository
import com.planify.planifyspring.main.features.profiles.data.models.ProfileModel
import com.planify.planifyspring.main.features.profiles.data.specifications.ProfileSearchSpecification
import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile
import com.planify.planifyspring.main.features.profiles.domain.repositories.ProfilesRepository
import com.planify.planifyspring.main.features.profiles.domain.schemas.CreateProfileSchema
import com.planify.planifyspring.main.features.profiles.domain.schemas.PatchProfileSchema
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import tools.jackson.databind.ObjectMapper

@Repository
class ProfilesRepositoryImpl(
    private val profilesJpaRepository: ProfilesJpaRepository,
    private val cacheManager: CacheManager,
    private val objectMapper: ObjectMapper
) : ProfilesRepository {
    private fun cache() = JsonCacheWrapper(cacheManager.getCache("profiles")!!, objectMapper)

    override fun getProfileById(userId: Long): Profile? {
        val cached = cache().getAs<ProfileCacheDTO>(userId.toString())
        if (cached != null) return cached.toEntity()

        return profilesJpaRepository.findByUserId(userId)?.toEntity()
            ?.also { cache().put(userId.toString(), ProfileCacheDTO.fromEntity(it)) }
    }

    override fun patchProfile(
        userId: Long,
        patch: PatchProfileSchema
    ) {
        cacheManager.getCache("profiles")?.evict(userId.toString())

        profilesJpaRepository.parchProfile(
            userId = userId,
            firstName = patch.firstName,
            lastName = patch.lastName,
            position = patch.position,
            department = patch.department,
            profileImageUrl = patch.profileImageUrl,
        )
    }

    override fun search(
        input: String,
        pageable: Pageable
    ): Page<Profile> {
        return profilesJpaRepository.findAll(ProfileSearchSpecification.searchProfile(input), pageable).map { it.toEntity() }
    }

    override fun createProfile(
        userId: Long,
        schema: CreateProfileSchema
    ): Profile {
        val profile = ProfileModel(
            userId = userId,
            firstName = schema.firstName,
            lastName = schema.lastName,
            position = schema.position,
            department = schema.department,
            profileImageUrl = schema.profileImageUrl ?: "https://dummyimage.com/512x512/ffae00/000000.png"
        )

        profilesJpaRepository.save(profile)

        return profile.toEntity()
            .also { cache().put(userId.toString(), ProfileCacheDTO.fromEntity(it)) }
    }
}
