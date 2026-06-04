package com.planify.planifyspring.main.features.fcm.domain.services_impl

import com.planify.planifyspring.main.features.fcm.domain.entities.FCMToken
import com.planify.planifyspring.main.features.fcm.domain.repositories.FCMTokenRepository
import com.planify.planifyspring.main.features.fcm.domain.services.FCMTokenService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FCMTokenServiceImpl(
    private val fcmTokenRepository: FCMTokenRepository
) : FCMTokenService {
    @Transactional(readOnly = true)
    override fun findTokensByUserId(userId: Long): List<FCMToken> {
        return fcmTokenRepository.findTokensByUserId(userId)
    }

    @Transactional
    override fun registerToken(userId: Long, token: String, platform: String) {
        fcmTokenRepository.saveToken(userId, token, platform)
    }

    @Transactional
    override fun deleteToken(token: String) {
        fcmTokenRepository.deleteByToken(token)
    }
}
