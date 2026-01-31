package com.planify.planifyspring.main.features.meetings.domain.services_impl

import com.planify.planifyspring.main.features.meetings.domain.entities.Meeting
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingContext
import com.planify.planifyspring.main.features.meetings.domain.repositories.MeetingInvitesRepository
import com.planify.planifyspring.main.features.meetings.domain.repositories.MeetingsRepository
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingPatchSchema
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import com.planify.planifyspring.main.features.profiles.domain.services.ProfilesService
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MeetingsServiceImpl(
    val meetingsRepository: MeetingsRepository,
    val meetingInvitesRepository: MeetingInvitesRepository,
    val profilesService: ProfilesService
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
        val meeting = meetingsRepository.createMeeting(
            ownerId = ownerId,
            name = name,
            description = description,
            location = location,
            startsAt = startsAt,
            duration = duration
        )

        if (!inviteUserIds.isNullOrEmpty()) {
            for (userId in inviteUserIds) {
                meetingInvitesRepository.createInvite(
                    meetingId = meeting.id,
                    senderId = ownerId,
                    targetUserId = userId
                )
            }
        }

        return meeting
    }

    override fun patchMeeting(  // TODO: Check if updated is owner
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
    ): Map<Instant, List<MeetingContext>> {
        val meetings = meetingsRepository.getUserDailyMeetingsWithParticipantIds(userId, startAt, endAt)

        return meetings.mapValues { (_, meetings) -> meetings.map { meetingInfo -> MeetingContext(
            meeting = meetingInfo.meeting,
            participants = meetingInfo.participantIds.map { profilesService.getProfileById(it) },  // TODO: Optimise it via db request
            invites = meetingInvitesRepository.getMeetingInvites(meetingInfo.meeting.id)
        ) } }
    }

    override fun getUserDailyMeetingsShort(
        userId: Long,
        startAt: Instant,
        endAt: Instant
    ): Map<Instant, Int> {
        return meetingsRepository.getUserDailyMeetingsShort(userId, startAt, endAt)
    }
}
