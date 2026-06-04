package com.planify.planifyspring.main.features.actions.domain.events.listener

import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.events.ActionCreatedEvent
import com.planify.planifyspring.main.features.fcm.domain.services_impl.FCMNotificationsServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class ActionPushNotificationListener(
    private val pushNotificationService: FCMNotificationsServiceImpl,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun buildData(action: Action): Map<String, String> {
        val data = mutableMapOf(
            "actionId" to action.id,
            "actionType" to action.type,
        )

        val json = objectMapper.writeValueAsString(action.data)
        if (json.toByteArray().size <= 2048) data["data"] = json
        return data
    }

    private fun notificationTextFor(type: String): Pair<String, String>? = when (type) {
        // TODO: type -> localized message
        else -> null
    }

    private fun shouldSendNotification(action: Action): Boolean {
        return true
    }

    @Async("pushTaskExecutor")
    @EventListener
    fun onActionCreated(event: ActionCreatedEvent) {
        try {
            val action = event.action
            if (!shouldSendNotification(action)) return

            val data = buildData(action)
            val text = notificationTextFor(action.type)

            if (text != null) {
                pushNotificationService.sendNotification(event.userId, text.first, text.second, data)
            } else {
                pushNotificationService.sendData(event.userId, data)
            }
        } catch (e: Exception) {
            log.warn("Failed to send notification for action ${event.action.id}", e)
        }
    }
}
