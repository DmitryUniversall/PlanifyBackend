package com.planify.planifyspring.main.features.meetings.routing.dto

import com.planify.planifyspring.main.features.meetings.routing.MeetingDTO
import java.util.Date

data class GetMyMeetingsResponseDTO(
    val meetings: Map<Date, List<MeetingDTO>>
)