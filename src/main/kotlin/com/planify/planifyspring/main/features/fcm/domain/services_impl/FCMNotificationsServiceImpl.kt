package com.planify.planifyspring.main.features.fcm.domain.services_impl

import com.google.firebase.messaging.*
import com.planify.planifyspring.main.features.fcm.domain.services.FCMTokenService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FCMNotificationsServiceImpl(
    private val firebaseMessaging: FirebaseMessaging,
    private val fcmTokenService: FCMTokenService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun dispatch(
        userId: Long,
        notification: Notification?,
        data: Map<String, String>,
    ) {
        val tokens = fcmTokenService.findTokensByUserId(userId).map { it.token }
        if (tokens.isEmpty()) return

        val builder = MulticastMessage.builder()
            .addAllTokens(tokens)
            .putAllData(data)
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build()
            )

        if (notification != null) builder.setNotification(notification)

        val response = firebaseMessaging.sendEachForMulticast(builder.build())

        if (response.failureCount > 0) {
            response.responses.forEachIndexed { i, r ->
                if (!r.isSuccessful) {
                    val code = r.exception?.messagingErrorCode

                    if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                        fcmTokenService.deleteToken(tokens[i])
                    } else {
                        log.warn("FCM send failed for token #{}: {}", i, r.exception?.message)
                    }
                }
            }
        }
    }

    fun sendData(
        userId: Long,
        data: Map<String, String>,
    ) {
        dispatch(userId, notification = null, data = data)
    }

    fun sendNotification(
        userId: Long,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
    ) {
        val notification = Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build()

        dispatch(userId, notification = notification, data = data)
    }
}
