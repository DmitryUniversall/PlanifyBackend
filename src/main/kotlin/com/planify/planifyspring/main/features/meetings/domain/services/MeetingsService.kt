package com.planify.planifyspring.main.features.meetings.domain.services

import com.planify.planifyspring.main.features.meetings.domain.entities.Meeting
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingContext
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingPatchSchema
import java.time.Instant

interface MeetingsService {
    fun createMeeting(
        ownerId: Long,
        name: String,
        description: String,
        location: String,
        startsAt: Instant,
        duration: Int,
        inviteUserIds: List<Long>?
    ): Meeting

    fun patchMeeting(
        meetingId: Long,
        updaterId: Long,
        patch: MeetingPatchSchema
    )

    fun getUserDailyMeetingsWithContext(
        userId: Long,
        startAt: Instant,
        endAt: Instant
    ): Map<Instant, List<MeetingContext>>

    fun getUserDailyMeetingsShort(
        userId: Long,
        startAt: Instant,
        endAt: Instant
    ): Map<Instant, Int>
}
