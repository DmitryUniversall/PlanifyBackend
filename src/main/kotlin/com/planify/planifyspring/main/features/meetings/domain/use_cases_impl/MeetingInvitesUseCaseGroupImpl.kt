package com.planify.planifyspring.main.features.meetings.domain.use_cases_impl

import com.planify.planifyspring.core.exceptions.NotFoundAppError
import com.planify.planifyspring.main.exceptions.generics.BadRequestHttpException
import com.planify.planifyspring.main.exceptions.generics.ForbiddenHttpException
import com.planify.planifyspring.main.exceptions.generics.NotFoundHttpException
import com.planify.planifyspring.main.features.meetings.domain.entities.Meeting
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInvite
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInviteStatus
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingInvitesService
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import com.planify.planifyspring.main.features.meetings.domain.use_cases.MeetingInvitesUseCaseGroup
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class MeetingInvitesUseCaseGroupImpl(
    val meetingInvitesService: MeetingInvitesService,
    val meetingService: MeetingsService,
) : MeetingInvitesUseCaseGroup {
    override fun createInvite(
        meetingId: Long,
        senderId: Long,
        targetId: Long
    ): MeetingInvite {
        val invites = getMeetingInvites(meetingId, senderId)
        if (invites.firstOrNull { it.targetId == targetId } != null) throw BadRequestHttpException("Cannot invite user: target already has an invite to this meeting")

        val meeting: Meeting
        try {
            meeting = meetingService.getMeetingById(meetingId)
        } catch (_: NotFoundAppError) {
            throw NotFoundHttpException("Cannot invite user: Meeting was not found")
        }

        if (senderId != meeting.ownerId) throw ForbiddenHttpException("Cannot invite user: you are not owner of this meeting")

        if (meetingService.isUserParticipant(targetId, meetingId)) throw BadRequestHttpException("Cannot invite user: target already participant of this meeting")

        return meetingInvitesService.createInvite(meetingId, senderId, targetId)
    }

    override fun getInvite(inviteUuid: String, requesterId: Long): MeetingInvite {
        val invite: MeetingInvite

        try {
            invite = meetingInvitesService.getInvite(inviteUuid)
        } catch (_: NotFoundAppError) {
            throw NotFoundHttpException("Invite was not found")
        }

        if (
            requesterId != invite.senderId &&
            requesterId != invite.targetId &&
            !meetingService.isUserParticipant(requesterId, invite.meetingId)
        ) throw ForbiddenHttpException("Cannot get invite info: you are not participant of this meeting")

        return invite
    }

    override fun getMeetingInvites(meetingId: Long, requesterId: Long): List<MeetingInvite> {
        val invites = meetingInvitesService.getMeetingInvites(meetingId)
        if (!meetingService.isUserParticipant(requesterId, meetingId)) throw ForbiddenHttpException("Cannot get invites info: you are not participant of this meeting")

        return invites
    }

    override fun acceptInvite(inviteUuid: String, requesterId: Long) {
        val invite = getInvite(inviteUuid, requesterId)
        if (invite.targetId != requesterId) throw ForbiddenHttpException("Cannot accept invite: you are not target of this invite")

        if (
            invite.status == MeetingInviteStatus.ACCEPTED ||
            invite.status == MeetingInviteStatus.REJECTED
        ) throw BadRequestHttpException("Invite has already been replied")

        meetingInvitesService.acceptInvite(invite)
    }

    override fun rejectInvite(inviteUuid: String, requesterId: Long) {
        val invite = getInvite(inviteUuid, requesterId)  // Also checks if invite exists and user can access its info
        if (invite.targetId != requesterId) throw ForbiddenHttpException("Cannot reject invite: you are not target of this invite")

        if (
            invite.status == MeetingInviteStatus.ACCEPTED ||
            invite.status == MeetingInviteStatus.REJECTED
        ) throw BadRequestHttpException("Invite has already been replied")

        meetingInvitesService.rejectInvite(invite)
    }

    override fun requestRescheduleInvite(inviteUuid: String, rescheduleTo: Instant, requesterId: Long) {
        val invite = getInvite(inviteUuid, requesterId)  // Also checks if invite exists and user can access its info
        if (invite.targetId != requesterId) throw ForbiddenHttpException("Cannot request reschedule: you are not target of this invite")

        if (
            invite.status == MeetingInviteStatus.ACCEPTED ||
            invite.status == MeetingInviteStatus.REJECTED
        ) throw BadRequestHttpException("Invite has already been replied")

        meetingInvitesService.requestRescheduleInvite(invite, rescheduleTo)
    }

    override fun responseRescheduleInvite(inviteUuid: String, shouldReschedule: Boolean, requesterId: Long) {
        val invite = getInvite(inviteUuid, requesterId)  // Also checks if invite exists and user can access its info
        if (invite.senderId != requesterId) throw ForbiddenHttpException("Cannot response reschedule: you are not sender of this invite")

        if (invite.status != MeetingInviteStatus.RESCHEDULE_REQUESTED) throw BadRequestHttpException("Reschedule is not requested")

        meetingInvitesService.responseRescheduleInvite(invite, shouldReschedule)
    }
}
