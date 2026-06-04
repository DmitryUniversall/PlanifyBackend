package com.planify.planifyspring.main.features.auth.domain.events.listeners

import com.planify.planifyspring.main.features.auth.domain.events.ConfirmationEmailRequestedEvent
import com.planify.planifyspring.main.features.email.domain.services.EmailService
import org.springframework.context.MessageSource
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.thymeleaf.context.Context
import java.util.*

@Component
class ConfirmationEmailListener(
    private val emailService: EmailService,
    private val messageSource: MessageSource
) {
    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ConfirmationEmailRequestedEvent) {
        val locale = event.locale ?: Locale.ENGLISH

        val context = Context(locale).apply {
            setVariable("verificationCode", event.code)
            setVariable("firstName", event.firstName)
            setVariable("expiryMinutes", event.expiryMinutes)
        }

        val subject = messageSource.getMessage(
            "email.registration.subject",
            null,
            locale,
        )

        emailService.sendFormattedHtmlMessage(event.email, subject, "auth/confirmation", context)
    }
}
