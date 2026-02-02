package com.planify.planifyspring.main.features.meetings.domain.services_impl

import com.planify.planifyspring.main.common.utils.ObjectMapperHelper
import com.planify.planifyspring.main.exceptions.generics.BadRequestHttpException
import com.planify.planifyspring.main.exceptions.generics.ForbiddenHttpException
import com.planify.planifyspring.main.exceptions.generics.NotFoundHttpException
import com.planify.planifyspring.main.features.actions.domain.services.ActionsService
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInvite
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInviteStatus
import com.planify.planifyspring.main.features.meetings.domain.repositories.MeetingInvitesRepository
import com.planify.planifyspring.main.features.meetings.domain.schemas.InviteRescheduleStatusDataScheme
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingInviteParchSchema
import com.planify.planifyspring.main.features.meetings.domain.schemas.actions.*
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingInvitesService
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class MeetingInvitesServiceImpl(
    val meetingInvitesRepository: MeetingInvitesRepository,
    val meetingService: MeetingsService,
    val actionsService: ActionsService,
    val objectMapperHelper: ObjectMapperHelper
) : MeetingInvitesService {
    override fun createInvite(
        meetingId: Long,
        senderId: Long,
        targetId: Long
    ): MeetingInvite {
        val invites = getMeetingInvites(meetingId, senderId)
        if (invites.firstOrNull { it.targetId == targetId } != null) throw BadRequestHttpException("Cannot invite user: target already has an invite to this meeting")

        val meeting = meetingService.getMeetingById(meetingId, senderId) ?: throw NotFoundHttpException("Meeting was not found")
        if (senderId != meeting.ownerId) throw ForbiddenHttpException("Cannot invite user: you are not owner of this meeting")

        if (meetingService.isUserParticipant(targetId, meetingId)) throw BadRequestHttpException("Cannot invite user: target already participant of this meeting")

        val invite = meetingInvitesRepository.createInvite(meetingId, senderId, targetId)

        actionsService.createAction(
            scope = "users:$targetId",
            type = "meetings:invited",
            data = UserActionInvitedToMeetingSchema(
                senderId = senderId,
                targetId = targetId,
                meetingId = meetingId,
                inviteUuid = invite.uuid,
                createdAt = invite.createdAt
            )
        )

        actionsService.createAction(
            scope = "meeting:$meetingId",
            type = "meetings:invited",
            data = MeetingActionUserInvitedSchema(
                senderId = senderId,
                targetId = meetingId,
                meetingId = meetingId,
                inviteUuid = invite.uuid,
                createdAt = invite.createdAt
            )
        )

        return invite
    }

    override fun getInvite(inviteUuid: String, requesterId: Long): MeetingInvite {
        val invite = meetingInvitesRepository.getInvite(inviteUuid) ?: throw NotFoundHttpException("Invite was not found")
        if (
            requesterId != invite.senderId &&
            requesterId != invite.targetId &&
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
        if (invite.targetId != requesterId) throw ForbiddenHttpException("Cannot accept invite: you are not target of this invite")

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

        meetingService.createMeetingParticipant(invite.meetingId, invite.targetId)

        actionsService.createAction(
            scope = "users:${invite.senderId}",
            type = "meetings:invite_status_updated",
            data = UserActionInviteStatusUpdatedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                oldStatus = invite.status,  // Still contain old status!
                newStatus = MeetingInviteStatus.ACCEPTED,
            )
        )

        actionsService.createAction(
            scope = "meeting:${invite.meetingId}",
            type = "meetings:invite_status_updated",
            data = MeetingActionInviteStatusUpdatedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                oldStatus = invite.status,  // Still contain old status!
                newStatus = MeetingInviteStatus.ACCEPTED
            )
        )

        actionsService.createAction(
            scope = "meeting:${invite.meetingId}",
            type = "meetings:new_participant",
            data = MeetingActionNewParticipantSchema(
                meetingId = invite.meetingId,
                newParticipantId = invite.targetId,
                joinedAt = Instant.now()
            )
        )
    }

    override fun rejectInvite(inviteUuid: String, requesterId: Long) {
        val invite = getInvite(inviteUuid, requesterId)  // Also checks if invite exists and user can access its info
        if (invite.targetId != requesterId) throw ForbiddenHttpException("Cannot reject invite: you are not target of this invite")

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

        actionsService.createAction(
            scope = "users:${invite.senderId}",
            type = "meetings:invite_status_updated",
            data = UserActionInviteStatusUpdatedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                oldStatus = invite.status,  // Still contain old status!
                newStatus = MeetingInviteStatus.REJECTED,
            )
        )

        actionsService.createAction(
            scope = "meeting:${invite.meetingId}",
            type = "meetings:invite_status_updated",
            data = MeetingActionInviteStatusUpdatedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                oldStatus = invite.status,  // Still contain old status!
                newStatus = MeetingInviteStatus.REJECTED
            )
        )
    }

    override fun requestRescheduleInvite(inviteUuid: String, rescheduleTo: Instant, requesterId: Long) {
        val invite = getInvite(inviteUuid, requesterId)  // Also checks if invite exists and user can access its info
        if (invite.targetId != requesterId) throw ForbiddenHttpException("Cannot request reschedule: you are not target of this invite")

        if (
            invite.status == MeetingInviteStatus.ACCEPTED ||
            invite.status == MeetingInviteStatus.REJECTED
        ) throw BadRequestHttpException("Invite has already been replied")

        meetingInvitesRepository.updateInvite(
            inviteUuid = inviteUuid,
            patch = MeetingInviteParchSchema(
                status = MeetingInviteStatus.RESCHEDULE_REQUESTED,
                statusData = InviteRescheduleStatusDataScheme(
                    rescheduleTo = rescheduleTo
                )
            )
        )

        actionsService.createAction(
            scope = "users:${invite.senderId}",
            type = "meetings:invite_reschedule_requested",
            data = UserActionInviteRescheduleRequestedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                rescheduleTo = rescheduleTo
            )
        )

        actionsService.createAction(
            scope = "meeting:${invite.meetingId}",
            type = "meetings:invite_reschedule_requested",
            data = MeetingActionInviteRescheduleRequestedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                rescheduleTo = rescheduleTo
            )
        )
    }

    override fun responseRescheduleInvite(inviteUuid: String, shouldReschedule: Boolean, requesterId: Long) {
        val invite = getInvite(inviteUuid, requesterId)  // Also checks if invite exists and user can access its info
        if (invite.senderId != requesterId) throw ForbiddenHttpException("Cannot response reschedule: you are not sender of this invite")

        if (invite.status != MeetingInviteStatus.RESCHEDULE_REQUESTED) throw BadRequestHttpException("Reschedule is not requested")

        meetingInvitesRepository.updateInvite(
            inviteUuid = inviteUuid,
            patch = MeetingInviteParchSchema(
                status = MeetingInviteStatus.PENDING
            )
        )

        if (shouldReschedule) {
            @Suppress("UNCHECKED_CAST")  // TODO: Refactor it
            val rescheduleTo = objectMapperHelper.convertFromStringsMap(invite.statusData!! as Map<String, String>, InviteRescheduleStatusDataScheme::class.java).rescheduleTo
            meetingService.rescheduleMeeting(meetingId = invite.meetingId, rescheduleTo = rescheduleTo, requesterId = requesterId)
        }

        actionsService.createAction(
            scope = "users:${invite.targetId}",
            type = "meetings:invite_reschedule_responded",
            data = UserActionInviteRescheduleRespondedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                shouldReschedule = shouldReschedule
            )
        )

        actionsService.createAction(
            scope = "meeting:${invite.meetingId}",
            type = "meetings:invite_reschedule_responded",
            data = MeetingActionInviteRescheduleRespondedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                shouldReschedule = shouldReschedule
            )
        )
    }
}
