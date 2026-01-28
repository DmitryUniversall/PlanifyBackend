package com.planify.planifyspring.main.features.profile.domain.services_impl

import com.planify.planifyspring.core.exceptions.NotFoundAppError
import com.planify.planifyspring.main.common.utils.JsonCacheWrapper
import com.planify.planifyspring.main.exceptions.generics.NotFoundHttpException
import com.planify.planifyspring.main.features.profile.domain.entiries.Profile
import com.planify.planifyspring.main.features.profile.domain.repositories.ProfilesRepository
import com.planify.planifyspring.main.features.profile.domain.services.ProfilesService
import com.planify.planifyspring.main.features.profile.domain.utils.ProfilePatchBuilder
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Service
class ProfilesServiceImpl(
    private val profilesRepository: ProfilesRepository,
    private val cacheManager: CacheManager,
    private val objectMapper: ObjectMapper
) : ProfilesService {
    override fun getProfileById(userId: Long): Profile {
        val cache = JsonCacheWrapper(cacheManager.getCache("profiles:$userId")!!, objectMapper)
        val cached = cache.getAs<Profile>("profiles:$userId")
        if (cached != null) return cached

        return (profilesRepository.getProfileById(userId) ?: throw NotFoundHttpException("Profile for this user was not found"))
            .also { profile -> cache.put("profiles:$userId", profile) }
    }

    override fun updateProfile(profile: Profile) {
        val cache = cacheManager.getCache("profiles:${profile.userId}")!!
        cache.evict("profiles:${profile.userId}")

        try {
            profilesRepository.updateProfile(profile)
        } catch (_: NotFoundAppError) {  // TODO: Do (select -> modify) to throw NotFoundAppError or keep it like this?
            throw NotFoundHttpException("Profile for this user was not found")
        }
    }

    override fun patchProfile(userId: Long, builderFunc: ProfilePatchBuilder.() -> Unit) {
        val cache = cacheManager.getCache("profiles:${userId}")!!
        cache.evict("profiles:${userId}")

        val patch = ProfilePatchBuilder().apply(builderFunc).build()

        try {
            return profilesRepository.patchProfile(userId, patch)
        } catch (_: NotFoundAppError) {
            throw NotFoundHttpException("Profile for this user was not found")
        }
    }
}
