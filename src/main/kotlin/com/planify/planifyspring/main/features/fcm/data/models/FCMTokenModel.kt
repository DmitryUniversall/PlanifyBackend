package com.planify.planifyspring.main.features.fcm.data.models

import com.planify.planifyspring.main.features.fcm.domain.entities.FCMToken
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "fcm_tokens")
open class FCMTokenModel(
    @Id
    @Column(nullable = false, unique = true, length = 512)
    open var token: String,

    @Column(nullable = false)
    open var userId: Long,

    @Column(nullable = false)
    open var platform: String = "android",

    @Column(nullable = false)
    open var createdAt: Instant,

    @Column(nullable = false)
    open var updatedAt: Instant
) {
    fun toEntity(): FCMToken = FCMToken(
        token = token,
        userId = userId,
        platform = platform,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
