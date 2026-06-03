package com.planify.planifyspring

import com.planify.planifyspring.main.features.email.domain.config.EmailServiceConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(EmailServiceConfiguration::class)
@SpringBootApplication
class PlanifySpringApplication

fun main(args: Array<String>) {
	runApplication<PlanifySpringApplication>(*args)
}
