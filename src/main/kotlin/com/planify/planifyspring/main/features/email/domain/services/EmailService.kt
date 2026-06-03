package com.planify.planifyspring.main.features.email.domain.services

import org.thymeleaf.context.Context
import java.util.concurrent.CompletableFuture

interface EmailService {
    fun sendHtmlMessage(to: String, subject: String, html: String): CompletableFuture<Void>
    fun sendHtmlResourceMessage(to: String, subject: String, resourcePath: String): CompletableFuture<Void>
    fun sendFormattedHtmlMessage(to: String, subject: String, filename: String, context: Context): CompletableFuture<Void>
}
