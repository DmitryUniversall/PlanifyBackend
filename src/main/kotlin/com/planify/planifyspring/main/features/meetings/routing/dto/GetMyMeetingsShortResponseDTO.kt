package com.planify.planifyspring.main.features.meetings.routing.dto

import java.time.Instant


data class GetMyMeetingsShortResponseDTO(
    val meetings: Map<Instant, Int>
)
