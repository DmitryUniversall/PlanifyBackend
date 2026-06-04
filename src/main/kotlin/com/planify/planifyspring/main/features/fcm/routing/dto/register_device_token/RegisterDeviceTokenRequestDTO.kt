package com.planify.planifyspring.main.features.fcm.routing.dto.register_device_token

data class RegisterDeviceTokenRequestDTO(
    val token: String,
    val platform: String = "android",
)
