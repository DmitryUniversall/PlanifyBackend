package com.planify.planifyspring.main.features.email.domain.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "service.email")
data class EmailServiceConfiguration(
    val from: String,
)
