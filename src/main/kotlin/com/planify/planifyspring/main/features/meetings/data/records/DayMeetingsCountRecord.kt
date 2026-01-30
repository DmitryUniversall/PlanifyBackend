package com.planify.planifyspring.main.features.meetings.data.records

import java.time.Instant


data class DayMeetingsCountRecord(
    val date: Instant,
    val count: Int
)
