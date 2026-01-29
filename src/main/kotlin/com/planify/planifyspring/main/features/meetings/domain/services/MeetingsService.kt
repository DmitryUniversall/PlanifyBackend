package com.planify.planifyspring.main.features.meetings.domain.services

import com.planify.planifyspring.main.features.meetings.domain.entities.Meeting
import java.util.*

interface MeetingsService {
    fun createMeeting(
        ownerId: Long,
        name: String,
        description: String,
        location: String,
        startsAt: Date,
        duration: Int,
        inviteUserIds: List<Long>?
    ): Meeting

    fun getUserMeetings(
        userId: Long,
        dateStart: Date?,
        endDate: Date?
    ): Map<Date, List<Meeting>>

    fun getUserMeetingsShort(
        userId: Long,
        dateStart: Date?,
        endDate: Date?
    ): Map<Date, Int>
}
