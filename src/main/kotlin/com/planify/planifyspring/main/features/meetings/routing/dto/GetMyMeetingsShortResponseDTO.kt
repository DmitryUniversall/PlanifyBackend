package com.planify.planifyspring.main.features.meetings.routing.dto

import java.util.*


data class GetMyMeetingsShortResponseDTO(
    val meetings: Map<Date, Int>
)
