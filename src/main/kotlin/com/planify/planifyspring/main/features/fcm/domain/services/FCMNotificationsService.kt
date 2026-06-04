package com.planify.planifyspring.main.features.fcm.domain.services

interface FCMNotificationsService {
    fun sendData(userId: Long, data: Map<String, String>)

    fun sendNotification(userId: Long, title: String, body: String, data: Map<String, String> = emptyMap())
}
