package com.planify.planifyspring.main.features.meetings.routing.dto.request_reschedule

import java.time.LocalDateTime

data class RequestRescheduleRequestDTO(
    val rescheduleTo: LocalDateTime
)
