package com.planify.planifyspring.main.features.meetings.domain.services_impl

import com.planify.planifyspring.main.features.meetings.domain.entities.Meeting
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingContext
import com.planify.planifyspring.main.features.meetings.domain.repositories.MeetingsRepository
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingPatchSchema
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MeetingsServiceImpl(
    val meetingsRepository: MeetingsRepository
) : MeetingsService {
    override fun createMeeting(
        ownerId: Long,
        name: String,
        description: String,
        location: String,
        startsAt: Instant,
        duration: Int,
        inviteUserIds: List<Long>?
    ): Meeting {
        TODO("Not yet implemented")
    }

    override fun patchMeeting(
        meetingId: Long,
        updaterId: Long,
        patch: MeetingPatchSchema
    ) {
        meetingsRepository.patchMeeting(meetingId, patch)
    }

    override fun getUserDailyMeetingsWithContext(
        userId: Long,
        startAt: Instant,
        endAt: Instant
    ): Map<Instant, MeetingContext> {
        TODO("Not yet implemented")
    }

    override fun getUserDailyMeetingsShort(
        userId: Long,
        startAt: Instant,
        endAt: Instant
    ): Map<Instant, Int> {
        return meetingsRepository.getUserDailyMeetingsShort(userId, startAt, endAt)
    }
}
