package com.planify.planifyspring.main.features.profiles.data.dto

import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile
import java.io.Serializable

data class ProfileCacheDTO(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val position: String?,
    val department: String?,
    val profileImageUrl: String
) : Serializable {
    companion object {
        fun fromEntity(entity: Profile): ProfileCacheDTO = ProfileCacheDTO(
            userId = entity.userId,
            firstName = entity.firstName,
            lastName = entity.lastName,
            position = entity.position,
            department = entity.department,
            profileImageUrl = entity.profileImageUrl
        )
    }

    fun toEntity(): Profile = Profile(
        userId = userId,
        firstName = firstName,
        lastName = lastName,
        position = position,
        department = department,
        profileImageUrl = profileImageUrl
    )
}
