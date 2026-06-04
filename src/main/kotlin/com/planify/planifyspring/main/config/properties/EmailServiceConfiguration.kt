package com.planify.planifyspring.main.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "service.email")
data class EmailServiceConfiguration(
    val from: String,
)
