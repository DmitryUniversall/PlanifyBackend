package com.planify.planifyspring.main.features.fcm.domain.repositories

import com.planify.planifyspring.main.features.fcm.domain.entities.FCMToken

interface FCMTokenRepository {
    fun saveToken(userId: Long, token: String, platform: String)

    fun deleteByToken(token: String)

    fun findTokensByUserId(userId: Long): List<FCMToken>
}
