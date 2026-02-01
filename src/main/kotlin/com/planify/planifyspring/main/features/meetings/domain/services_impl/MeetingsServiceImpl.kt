package com.planify.planifyspring.main.features.meetings.domain.services_impl

import com.planify.planifyspring.main.features.meetings.domain.entities.Meeting
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingParticipant
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingWithParticipantIds
import com.planify.planifyspring.main.features.meetings.domain.repositories.MeetingsRepository
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingPatchSchema
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
        duration: Int
    ): Meeting {
        val meeting = meetingsRepository.createMeeting(
            ownerId = ownerId,
            name = name,
            description = description,
            location = location,
            startsAt = startsAt,
            duration = duration
        )

        createMeetingParticipant(
            meetingId = meeting.id,
            userId = ownerId
        )

        return meeting
    }

    override fun createMeetingParticipant(
        meetingId: Long,
        userId: Long
    ): MeetingParticipant {
        return meetingsRepository.createMeetingParticipant(
            meetingId = meetingId,
            userId = userId
        )
    }

    override fun isUserParticipant(userId: Long, meetingId: Long): Boolean {
        return meetingsRepository.isUserParticipant(userId, meetingId)
    }

    @Transactional
    override fun rescheduleMeeting(meetingId: Long, rescheduleTo: Instant, requesterId: Long) {
        patchMeeting(
            meetingId = meetingId,
            updaterId = requesterId,
            patch = MeetingPatchSchema(
                startsAt = rescheduleTo
            )
        )
    }

    override fun getMeetingById(
        meetingId: Long,
        requesterId: Long
    ): Meeting? {
        return meetingsRepository.getMeetingById(meetingId)  // TODO: Check if requester is meeting participant
    }

    @Transactional
    override fun patchMeeting(  // TODO: Check if updated is owner
        meetingId: Long,
        updaterId: Long,
        patch: MeetingPatchSchema
    ) {
        meetingsRepository.patchMeeting(meetingId, patch)
    }

    override fun getUserDailyMeetingsWithParticipantIds(
        userId: Long,
        startAt: Instant,
        endAt: Instant
    ): Map<Instant, List<MeetingWithParticipantIds>> {
        return meetingsRepository.getUserDailyMeetingsWithParticipantIds(userId, startAt, endAt)
    }

    override fun getUserDailyMeetingsShort(
        userId: Long,
        startAt: Instant,
        endAt: Instant
    ): Map<Instant, Long> {
        return meetingsRepository.getUserDailyMeetingsShort(userId, startAt, endAt)
    }
}
