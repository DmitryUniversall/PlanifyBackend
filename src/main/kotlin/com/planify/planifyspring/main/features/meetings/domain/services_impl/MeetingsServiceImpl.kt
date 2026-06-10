package com.planify.planifyspring.main.features.meetings.domain.services_impl

import com.planify.planifyspring.core.utils.atStartOfAnHour
import com.planify.planifyspring.main.features.actions.domain.services.ActionsService
import com.planify.planifyspring.main.features.meetings.domain.entities.Meeting
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingParticipant
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingWithParticipantIds
import com.planify.planifyspring.main.features.meetings.domain.exceptions.MeetingInPastAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.MeetingNotFoundAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.MeetingTimeConflictAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.RescheduleToPastAppError
import com.planify.planifyspring.main.features.meetings.domain.policies.MeetingPolicy
import com.planify.planifyspring.main.features.meetings.domain.repositories.MeetingsRepository
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingActionMeetingUpdatedSchema
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingPatchSchema
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class MeetingsServiceImpl(
    val actionsService: ActionsService,
    val meetingsRepository: MeetingsRepository,
    val meetingPolicy: MeetingPolicy
) : MeetingsService {
    @Transactional
    override fun createMeeting(
        ownerId: Long,
        name: String,
        description: String,
        location: String,
        startsAt: Instant,
        duration: Int
    ): Meeting {
        val start = startsAt.atStartOfAnHour()
        val end = start.plusSeconds(duration * 3600L)

        if (start < Instant.now()) throw MeetingInPastAppError()
        if (meetingsRepository.userHasMeetingsBetween(userId = ownerId, startAt = start, endAt = end)) throw MeetingTimeConflictAppError()

        val meeting = meetingsRepository.createMeeting(
            ownerId = ownerId,
            name = name,
            description = description,
            location = location,
            startsAt = start,
            duration = duration
        )

        createMeetingParticipant(
            meetingId = meeting.id,
            userId = ownerId
        )

        return meeting
    }

    @Transactional(readOnly = true)
    override fun getMeetingById(meetingId: Long): Meeting {
        return meetingsRepository.getMeetingById(meetingId) ?: throw MeetingNotFoundAppError()
    }

    @Transactional
    override fun patchMeeting(meetingId: Long, patch: MeetingPatchSchema, requesterId: Long) {
        val meeting = meetingsRepository.getMeetingById(meetingId) ?: throw MeetingNotFoundAppError()

        meetingPolicy.assertIsOwner(requesterId, meeting)
        if (meeting.startsAt < Instant.now()) throw RescheduleToPastAppError()

        meetingsRepository.patchMeeting(meetingId, patch)

        actionsService.createAction(
            scope = "meeting:$meetingId",
            type = "meetings:meeting:updated",
            data = MeetingActionMeetingUpdatedSchema(
                updaterId = requesterId,
                meetingId = meetingId,
                patch = patch,
                updatedAt = Instant.now()
            )
        )
    }

    @Transactional(readOnly = true)
    override fun getUserDailyMeetingsWithParticipantIds(
        userId: Long,
        startAt: Instant,
        endAt: Instant
    ): Map<Instant, List<MeetingWithParticipantIds>> {
        return meetingsRepository.getUserDailyMeetingsWithParticipantIds(userId, startAt, endAt)
    }

    @Transactional(readOnly = true)
    override fun getUserDailyMeetingsShort(userId: Long, startAt: Instant, endAt: Instant): Map<Instant, Long> {
        return meetingsRepository.getUserDailyMeetingsShort(userId, startAt, endAt)
    }

    override fun createMeetingParticipant(meetingId: Long, userId: Long): MeetingParticipant {
        return meetingsRepository.createMeetingParticipant(
            meetingId = meetingId,
            userId = userId
        )
    }

    @Transactional
    override fun rescheduleMeeting(meetingId: Long, rescheduleTo: Instant, requesterId: Long) {
        val patch = MeetingPatchSchema(
            startsAt = rescheduleTo
        )

        val meeting = meetingsRepository.getMeetingById(meetingId) ?: throw MeetingNotFoundAppError()
        meetingPolicy.assertIsOwner(requesterId, meeting)

        meetingsRepository.patchMeeting(
            meetingId = meetingId,
            patch = patch
        )

        actionsService.createAction(
            scope = "meeting:$meetingId",
            type = "meetings:meeting:updated",
            data = MeetingActionMeetingUpdatedSchema(
                updaterId = requesterId,
                meetingId = meetingId,
                patch = patch,
                updatedAt = Instant.now()
            )
        )
    }

    @Transactional(readOnly = true)
    override fun getMeetingWithParticipantIds(meetingId: Long): MeetingWithParticipantIds {
        return meetingsRepository.getMeetingWithParticipantIds(meetingId) ?: throw MeetingNotFoundAppError()
    }

    @Transactional(readOnly = true)
    override fun isUserParticipant(userId: Long, meetingId: Long): Boolean {
        return meetingsRepository.isUserParticipant(userId, meetingId)
    }

    @Transactional(readOnly = true)
    override fun userHasMeetingsBetween(userId: Long, startAt: Instant, endAt: Instant): Boolean {
        return meetingsRepository.userHasMeetingsBetween(userId, startAt, endAt)
    }
}
