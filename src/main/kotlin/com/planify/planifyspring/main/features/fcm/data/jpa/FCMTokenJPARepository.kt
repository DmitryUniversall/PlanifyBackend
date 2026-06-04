package com.planify.planifyspring.main.features.fcm.data.jpa

import com.planify.planifyspring.main.features.fcm.data.models.FCMTokenModel
import org.springframework.data.jpa.repository.JpaRepository

interface FCMTokenJPARepository : JpaRepository<FCMTokenModel, String> {
    fun findAllByUserId(userId: Long): List<FCMTokenModel>

    fun deleteByToken(token: String): List<FCMTokenModel>
}
