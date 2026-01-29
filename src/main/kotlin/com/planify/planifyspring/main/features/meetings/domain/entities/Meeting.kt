package com.planify.planifyspring.main.features.meetings.domain.entities

import java.util.Date

data class Meeting(
    val id: Long,
    val ownerId: Long,
    val name: String,
    val description: String,
    val location: String,
    val startsAt: Date,
    val duration: Int
)
