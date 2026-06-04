package com.planify.planifyspring

import com.planify.planifyspring.main.config.properties.EmailServiceConfiguration
import com.planify.planifyspring.main.config.properties.FirebaseConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(EmailServiceConfiguration::class, FirebaseConfiguration::class)
@SpringBootApplication
class PlanifySpringApplication

fun main(args: Array<String>) {
    runApplication<PlanifySpringApplication>(*args)
}
