package com.planify.planifyspring.main.features.fcm.domain.services

import com.planify.planifyspring.main.features.fcm.domain.entities.FCMToken

interface FCMTokenService {
    fun findTokensByUserId(userId: Long): List<FCMToken>

    fun registerToken(userId: Long, token: String, platform: String)

    fun deleteToken(token: String)
}
