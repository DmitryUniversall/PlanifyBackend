package com.planify.planifyspring.main.features.actions.domain.events.listener

import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.events.ActionCreatedEvent
import com.planify.planifyspring.main.features.auth.domain.services.AuthService
import com.planify.planifyspring.main.features.fcm.domain.services_impl.FCMNotificationsServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.*

@Component
class ActionPushNotificationListener(
    private val pushNotificationService: FCMNotificationsServiceImpl,
    private val objectMapper: ObjectMapper,
    private val authService: AuthService,
    private val messageSource: MessageSource
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MAX_DATA_BYTES = 2048
    }

    private data class NotificationText(val title: String, val body: String)

    private fun buildData(action: Action): Map<String, String> {
        val data = mutableMapOf(
            "actionId" to action.id,
            "actionType" to action.type,
        )

        val json = objectMapper.writeValueAsString(action.data)
        val size = json.toByteArray(Charsets.UTF_8).size

        if (size <= MAX_DATA_BYTES) {
            data["data"] = json
        } else {
            log.warn("Action data too large to inline for action ${action.id} (${size} bytes > ${MAX_DATA_BYTES}), omitting 'data' field")
        }

        return data
    }

    private fun notificationTextFor(action: Action, locale: Locale): NotificationText? {
        val key = action.type.replace(':', '.')

        // TODO: Args
        val title = messageSource.getMessage("notifications.title.actions.$key", null, null, locale)
        val body = messageSource.getMessage("notifications.body.actions.$key", null, null, locale)

        return if (title != null && body != null) NotificationText(title, body) else null
    }

    private fun sendNotification(action: Action, userId: Long) {
        val locale = authService.getUserLocaleById(userId)

        val text = notificationTextFor(action, locale)
        if (text == null) {
            log.error("No localized notification text found for action type '{action.type}' (action {action.id}, user {userId}, locale {locale})")
            return
        }

        pushNotificationService.sendNotification(userId = userId, title = text.title, body = text.body, data = buildData(action))
    }

    private fun sendDataMessage(action: Action, userId: Long) {
        val data = buildData(action)
        pushNotificationService.sendData(userId = userId, data = data)
    }

    private fun isNotificationActionEvent(event: ActionCreatedEvent): Boolean {  // TODO
        return true
    }

    private fun isDataMessageActionEvent(event: ActionCreatedEvent): Boolean {  // TODO
        return false
    }

    @Async("pushTaskExecutor")
    @EventListener
    fun onActionCreated(event: ActionCreatedEvent) {
        try {
            when {
                isNotificationActionEvent(event) -> sendNotification(event.action, event.userId)
                isDataMessageActionEvent(event) -> sendDataMessage(event.action, event.userId)
                else -> log.debug("No push delivery for action ${event.action.id} (type ${event.action.type})")
            }
        } catch (e: Exception) {
            log.warn("Failed to send notification for action ${event.action.id}", e)
        }
    }
}
