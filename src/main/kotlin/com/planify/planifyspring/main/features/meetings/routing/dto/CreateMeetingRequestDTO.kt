package com.planify.planifyspring.main.features.meetings.routing.dto

import java.util.Date

data class CreateMeetingRequestDTO(
    val name: String,
    val description: String,
    val location: String,
    val startsAt: Date,
    val duration: Int,
    val inviteUserIds: List<Long>?
)
