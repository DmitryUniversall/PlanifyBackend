package com.planify.planifyspring.main.features.auth.domain.events.listeners

import com.planify.planifyspring.main.features.auth.domain.events.RecoverPasswordEmailRequestedEvent
import com.planify.planifyspring.main.features.email.domain.services.EmailService
import org.springframework.context.MessageSource
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.thymeleaf.context.Context
import java.util.*

@Component
class RecoverPasswordEmailListener(
    private val emailService: EmailService,
    private val messageSource: MessageSource
) {
    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: RecoverPasswordEmailRequestedEvent) {
        val context = Context().apply {
            setVariable("verificationCode", event.code)
            setVariable("expiryMinutes", 20)  // FIXME: Hardcoded
        }

        val subject = messageSource.getMessage(
            /* code = */ "email.registration.subject",
            /* args = */ null,
            /* locale = */ event.locale ?: Locale.getDefault(),
        )

        emailService.sendFormattedHtmlMessage(event.email, subject, "auth/recovery", context)
    }
}
