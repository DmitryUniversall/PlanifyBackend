package com.planify.planifyspring.main.features.email.domain.services_impl

import com.planify.planifyspring.main.config.properties.EmailServiceConfiguration
import com.planify.planifyspring.main.features.email.domain.services.EmailService
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.concurrent.CompletableFuture

@Service
class EmailServiceImpl(
    private val mailSender: JavaMailSender,
    private val config: EmailServiceConfiguration,
    private val templateEngine: TemplateEngine
) : EmailService {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async("mailExecutor")
    override fun sendHtmlMessage(to: String, subject: String, html: String): CompletableFuture<Void> {
        return try {
            val message = mailSender.createMimeMessage()

            MimeMessageHelper(message, true, "UTF-8").apply {
                setFrom(config.from)
                setTo(to)
                setSubject(subject)
                setText(html, true)
            }

            mailSender.send(message)

            CompletableFuture.completedFuture(null)
        } catch (ex: Exception) {
            log.error("Failed to send email for $to", ex)

            CompletableFuture.failedFuture(ex)
        }
    }

    @Async("mailExecutor")
    override fun sendHtmlResourceMessage(to: String, subject: String, resourcePath: String): CompletableFuture<Void> {
        val html = ClassPathResource(resourcePath).inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return sendHtmlMessage(to, subject, html)
    }

    @Async("mailExecutor")
    override fun sendFormattedHtmlMessage(to: String, subject: String, filename: String, context: Context): CompletableFuture<Void> {
        val html = templateEngine.process(filename, context)
        return sendHtmlMessage(to, subject, html)
    }
}
