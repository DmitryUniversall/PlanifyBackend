package com.planify.planifyspring.main.features.fcm.data.repositories_impl

import com.planify.planifyspring.main.features.fcm.data.jpa.FCMTokenJPARepository
import com.planify.planifyspring.main.features.fcm.data.models.FCMTokenModel
import com.planify.planifyspring.main.features.fcm.domain.entities.FCMToken
import com.planify.planifyspring.main.features.fcm.domain.repositories.FCMTokenRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class FCMTokenRepositoryImpl(
    private val jpa: FCMTokenJPARepository
) : FCMTokenRepository {
    override fun saveToken(userId: Long, token: String, platform: String) {
        jpa.save(FCMTokenModel(token = token, userId = userId, platform = platform, createdAt = Instant.now(), updatedAt = Instant.now()))
    }

    override fun deleteByToken(token: String) {
        jpa.deleteByToken(token)
    }

    override fun findTokensByUserId(userId: Long): List<FCMToken> {
        return jpa.findAllByUserId(userId).map { it.toEntity() }
    }
}
