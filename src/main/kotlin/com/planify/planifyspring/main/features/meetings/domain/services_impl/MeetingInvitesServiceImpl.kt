package com.planify.planifyspring.main.features.meetings.domain.services_impl

import com.planify.planifyspring.main.exceptions.generics.BadRequestHttpException
import com.planify.planifyspring.main.exceptions.generics.ForbiddenHttpException
import com.planify.planifyspring.main.exceptions.generics.NotFoundHttpException
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInvite
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInviteStatus
import com.planify.planifyspring.main.features.meetings.domain.repositories.MeetingInvitesRepository
import com.planify.planifyspring.main.features.meetings.domain.schemas.InviteRescheduleStatusDataScheme
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingInviteParchSchema
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingInvitesService
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class MeetingInvitesServiceImpl(
    val meetingInvitesRepository: MeetingInvitesRepository,
    val meetingService: MeetingsService
) : MeetingInvitesService {

    override fun createInvite(
        meetingId: Long,
        senderId: Long,
        targetUserId: Long
    ): MeetingInvite {
        val meeting = meetingService.getMeetingById(meetingId, senderId) ?: throw NotFoundHttpException("Meeting was not found")
        if (senderId != meeting.ownerId) throw ForbiddenHttpException("Cannot invite user: you are not owner of this meeting")

        return meetingInvitesRepository.createInvite(meetingId, senderId, targetUserId)
    }

    override fun getInvite(inviteUuid: String, requesterId: Long): MeetingInvite {
        val invite = meetingInvitesRepository.getInvite(inviteUuid) ?: throw NotFoundHttpException("Invite was not found")
        if (
            requesterId != invite.senderId &&
            requesterId != invite.targetUserId &&
            !meetingService.isUserParticipant(requesterId, invite.meetingId)
        ) throw ForbiddenHttpException("Cannot get invite info: you are not participant of this meeting")

        return invite
    }

    override fun getMeetingInvites(meetingId: Long, requesterId: Long): List<MeetingInvite> {
        val invites = meetingInvitesRepository.getMeetingInvites(meetingId)
        if (!meetingService.isUserParticipant(requesterId, meetingId)) throw ForbiddenHttpException("Cannot get invites info: you are not participant of this meeting")

        return invites
    }

    override fun acceptInvite(inviteUuid: String, requesterId: Long) {
        val invite = getInvite(inviteUuid, requesterId)  // Also checks if invite exists and user can access its info
        if (invite.targetUserId != requesterId) throw ForbiddenHttpException("Cannot accept invite: you are not target of this invite")

        if (
            invite.status == MeetingInviteStatus.ACCEPTED ||
            invite.status == MeetingInviteStatus.REJECTED
        ) throw BadRequestHttpException("Invite has already been replied")

        meetingInvitesRepository.updateInvite(
            inviteUuid = inviteUuid,
            patch = MeetingInviteParchSchema(
                status = MeetingInviteStatus.ACCEPTED
            )
        )

        // TODO: Send action to owner and meeting
    }

    override fun rejectInvite(inviteUuid: String, requesterId: Long) {
        val invite = getInvite(inviteUuid, requesterId)  // Also checks if invite exists and user can access its info
        if (invite.targetUserId != requesterId) throw ForbiddenHttpException("Cannot accept invite: you are not target of this invite")

        if (
            invite.status == MeetingInviteStatus.ACCEPTED ||
            invite.status == MeetingInviteStatus.REJECTED
        ) throw BadRequestHttpException("Invite has already been replied")

        meetingInvitesRepository.updateInvite(
            inviteUuid = inviteUuid,
            patch = MeetingInviteParchSchema(
                status = MeetingInviteStatus.REJECTED
            )
        )

        // TODO: Send action to owner and meeting
    }

    override fun requestRescheduleInvite(inviteUuid: String, rescheduleTo: Instant, requesterId: Long) {
        val invite = getInvite(inviteUuid, requesterId)  // Also checks if invite exists and user can access its info
        if (invite.targetUserId != requesterId) throw ForbiddenHttpException("Cannot accept invite: you are not target of this invite")

        if (
            invite.status == MeetingInviteStatus.ACCEPTED ||
            invite.status == MeetingInviteStatus.REJECTED
        ) throw BadRequestHttpException("Invite has already been replied")

        meetingInvitesRepository.updateInvite(
            inviteUuid = inviteUuid,
            patch = MeetingInviteParchSchema(
                status = MeetingInviteStatus.RESCHEDULE_REQUESTED,
                statusData = InviteRescheduleStatusDataScheme(
                    newDateTime = Instant.now()
                )
            )
        )

        // TODO: Send action to owner and meeting
    }
}
