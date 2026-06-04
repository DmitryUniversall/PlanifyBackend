package com.planify.planifyspring.main.features.meetings.domain.services_impl

import com.planify.planifyspring.main.common.utils.ObjectMapperHelper
import com.planify.planifyspring.main.features.actions.domain.services.ActionsService
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInvite
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInviteStatus
import com.planify.planifyspring.main.features.meetings.domain.exceptions.InviteAlreadyRepliedAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.InviteExpiredAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.InviteNotFoundAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.MeetingAlreadyStartedAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.MeetingTimeConflictAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.RescheduleNotRequestedAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.TargetAlreadyInvitedAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.TargetAlreadyParticipantAppError
import com.planify.planifyspring.main.features.meetings.domain.policies.MeetingInvitePolicy
import com.planify.planifyspring.main.features.meetings.domain.policies.MeetingPolicy
import com.planify.planifyspring.main.features.meetings.domain.repositories.MeetingInvitesRepository
import com.planify.planifyspring.main.features.meetings.domain.schemas.InviteRescheduleStatusDataScheme
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingInvitePatchSchema
import com.planify.planifyspring.main.features.meetings.domain.schemas.actions.*
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingInvitesService
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class MeetingInvitesServiceImpl(
    val meetingInvitesRepository: MeetingInvitesRepository,
    val actionsService: ActionsService,
    val meetingsService: MeetingsService,
    val objectMapperHelper: ObjectMapperHelper,
    val meetingPolicy: MeetingPolicy,
    val meetingInvitePolicy: MeetingInvitePolicy
) : MeetingInvitesService {

    private fun assertInviteNotReplied(invite: MeetingInvite) {
        if (invite.status == MeetingInviteStatus.ACCEPTED || invite.status == MeetingInviteStatus.REJECTED) {
            throw InviteAlreadyRepliedAppError()
        }
    }

    private fun assertInviteNotExpired(invite: MeetingInvite) {
        if (invite.expiresAt < Instant.now()) throw InviteExpiredAppError()
    }

    @Transactional(readOnly = true)
    override fun getInvite(inviteUuid: String): MeetingInvite {
        return meetingInvitesRepository.getInvite(inviteUuid) ?: throw InviteNotFoundAppError()
    }

    @Transactional(readOnly = true)
    override fun getMeetingInvites(meetingId: Long): List<MeetingInvite> {
        return meetingInvitesRepository.getMeetingInvites(meetingId)
    }

    @Transactional
    override fun createInvite(meetingId: Long, senderId: Long, targetId: Long): MeetingInvite {
        val meeting = meetingsService.getMeetingById(meetingId)

        meetingPolicy.assertIsOwner(senderId, meeting)

        if (getMeetingInvites(meetingId).any { it.targetId == targetId }) throw TargetAlreadyInvitedAppError()
        if (meetingsService.isUserParticipant(targetId, meetingId)) throw TargetAlreadyParticipantAppError()
        if (meeting.startsAt < Instant.now()) throw MeetingAlreadyStartedAppError()

        val invite = meetingInvitesRepository.createInvite(meetingId, senderId, targetId, expiresAt = meeting.startsAt)

        actionsService.createUserAction(
            userId = targetId,
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
            type = "meetings:meeting:invited",
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

    @Transactional
    override fun acceptInvite(inviteUuid: String, requesterId: Long) {
        val invite = getInvite(inviteUuid)
        meetingInvitePolicy.assertIsTarget(requesterId, invite)

        assertInviteNotReplied(invite)
        assertInviteNotExpired(invite)

        val meeting = meetingsService.getMeetingById(invite.meetingId)
        if (
            meetingsService.userHasMeetingsBetween(
                userId = invite.targetId,
                startAt = meeting.startsAt,
                endAt = meeting.startsAt.plusSeconds(meeting.duration * 3600L)
            )
        ) throw MeetingTimeConflictAppError()

        meetingInvitesRepository.updateInvite(
            inviteUuid = invite.uuid,
            patch = MeetingInvitePatchSchema(status = MeetingInviteStatus.ACCEPTED)
        )

        meetingsService.createMeetingParticipant(invite.meetingId, invite.targetId)

        actionsService.createUserAction(
            userId = invite.senderId,
            type = "meetings:invite_status_updated",
            data = UserActionInviteStatusUpdatedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                oldStatus = invite.status,
                newStatus = MeetingInviteStatus.ACCEPTED,
            )
        )

        actionsService.createAction(
            scope = "meeting:${invite.meetingId}",
            type = "meetings:meeting:invite_status_updated",
            data = MeetingActionInviteStatusUpdatedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                oldStatus = invite.status,
                newStatus = MeetingInviteStatus.ACCEPTED
            )
        )

        actionsService.createAction(
            scope = "meeting:${invite.meetingId}",
            type = "meetings:meeting:new_participant",
            data = MeetingActionNewParticipantSchema(
                meetingId = invite.meetingId,
                newParticipantId = invite.targetId,
                joinedAt = Instant.now()
            )
        )
    }

    @Transactional
    override fun rejectInvite(inviteUuid: String, requesterId: Long) {
        val invite = getInvite(inviteUuid)
        meetingInvitePolicy.assertIsTarget(requesterId, invite)

        assertInviteNotReplied(invite)
        assertInviteNotExpired(invite)

        meetingInvitesRepository.updateInvite(
            inviteUuid = invite.uuid,
            patch = MeetingInvitePatchSchema(status = MeetingInviteStatus.REJECTED)
        )

        actionsService.createUserAction(
            userId = invite.senderId,
            type = "meetings:invite_status_updated",
            data = UserActionInviteStatusUpdatedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                oldStatus = invite.status,
                newStatus = MeetingInviteStatus.REJECTED,
            )
        )

        actionsService.createAction(
            scope = "meeting:${invite.meetingId}",
            type = "meetings:meeting:invite_status_updated",
            data = MeetingActionInviteStatusUpdatedSchema(
                meetingId = invite.meetingId,
                senderId = invite.senderId,
                targetId = invite.targetId,
                inviteUuid = invite.uuid,
                updatedAt = Instant.now(),
                oldStatus = invite.status,
                newStatus = MeetingInviteStatus.REJECTED
            )
        )
    }

    @Transactional
    override fun requestRescheduleInvite(inviteUuid: String, rescheduleTo: Instant, requesterId: Long) {
        val invite = getInvite(inviteUuid)
        meetingInvitePolicy.assertIsTarget(requesterId, invite)

        assertInviteNotReplied(invite)
        assertInviteNotExpired(invite)

        meetingInvitesRepository.updateInvite(
            inviteUuid = invite.uuid,
            patch = MeetingInvitePatchSchema(
                status = MeetingInviteStatus.RESCHEDULE_REQUESTED,
                statusData = InviteRescheduleStatusDataScheme(rescheduleTo = rescheduleTo)
            )
        )

        actionsService.createUserAction(
            userId = invite.senderId,
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
            type = "meetings:meeting:invite_reschedule_requested",
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

    @Transactional
    override fun responseRescheduleInvite(inviteUuid: String, shouldReschedule: Boolean, requesterId: Long) {
        val invite = getInvite(inviteUuid)
        meetingInvitePolicy.assertIsSender(requesterId, invite)

        if (invite.status != MeetingInviteStatus.RESCHEDULE_REQUESTED) throw RescheduleNotRequestedAppError()
        assertInviteNotExpired(invite)

        meetingInvitesRepository.updateInvite(
            inviteUuid = invite.uuid,
            patch = MeetingInvitePatchSchema(status = MeetingInviteStatus.PENDING)
        )

        if (shouldReschedule) {
            @Suppress("UNCHECKED_CAST")
            val rescheduleTo = objectMapperHelper.convertFromStringsMap(
                invite.statusData!! as Map<String, String>,
                InviteRescheduleStatusDataScheme::class.java
            ).rescheduleTo
            meetingsService.rescheduleMeeting(meetingId = invite.meetingId, rescheduleTo = rescheduleTo)
        }

        actionsService.createUserAction(
            userId = invite.targetId,
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
            type = "meetings:meeting:invite_reschedule_responded",
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
