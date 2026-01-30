package com.planify.planifyspring.main.features.profiles.domain.services_impl

import com.planify.planifyspring.core.exceptions.NotFoundAppError
import com.planify.planifyspring.main.common.utils.JsonCacheWrapper
import com.planify.planifyspring.main.exceptions.generics.NotFoundHttpException
import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile
import com.planify.planifyspring.main.features.profiles.domain.repositories.ProfilesRepository
import com.planify.planifyspring.main.features.profiles.domain.schemas.ProfilePatchSchema
import com.planify.planifyspring.main.features.profiles.domain.services.ProfilesService
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

    override fun patchProfile(userId: Long, patch: ProfilePatchSchema) {
        val cache = cacheManager.getCache("profiles:${userId}")!!
        cache.evict("profiles:${userId}")

        try {
            return profilesRepository.patchProfile(userId, patch)
        } catch (_: NotFoundAppError) {  // TODO: Do (select -> modify) to throw NotFoundAppError or keep it like this?
            throw NotFoundHttpException("Profile for this user was not found")
        }
    }
}
