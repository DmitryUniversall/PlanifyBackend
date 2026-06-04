package com.planify.planifyspring.main.features.meetings.routing.dto.reschedule_request

import java.time.Instant

data class RescheduleRequestDTO(
    val rescheduleTo: Instant
)
