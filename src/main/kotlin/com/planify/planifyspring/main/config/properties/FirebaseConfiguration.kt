package com.planify.planifyspring.main.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "firebase")
data class FirebaseConfiguration(
    val credentialsLocation: String
)
