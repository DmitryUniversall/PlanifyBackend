package com.planify.planifyspring.main.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.planify.planifyspring.main.config.properties.FirebaseConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
class FirebaseConfig(
    private val resourceLoader: ResourceLoader,
    private val config: FirebaseConfiguration,
) {
    @Bean
    fun firebaseApp(): FirebaseApp {
        if (FirebaseApp.getApps().isNotEmpty()) return FirebaseApp.getInstance()

        val credentials = resourceLoader.getResource(config.credentialsLocation)
            .inputStream
            .use { GoogleCredentials.fromStream(it) }

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build()

        return FirebaseApp.initializeApp(options)
    }

    @Bean
    fun firebaseMessaging(app: FirebaseApp): FirebaseMessaging = FirebaseMessaging.getInstance(app)
}
