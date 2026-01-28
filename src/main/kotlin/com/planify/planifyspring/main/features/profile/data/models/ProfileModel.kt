package com.planify.planifyspring.main.features.profile.data.models

import com.planify.planifyspring.main.features.profile.domain.entiries.Profile
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "profiles")
open class ProfileModel(
    @Id
    @Column(nullable = false, unique = true)
    val userId: Long,

    val firstName: String,

    val lastName: String,

    val position: String,

    val department: String,

    val profileImageUrl: String
) {
    fun toEntity(): Profile {
        return Profile(
            userId = userId,
            firstName = firstName,
            lastName = lastName,
            position = position,
            department = department,
            profileImageUrl = profileImageUrl
        )
    }
}
