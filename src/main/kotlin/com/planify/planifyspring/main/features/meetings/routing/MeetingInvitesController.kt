package com.planify.planifyspring.main.features.meetings.routing

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessApplicationResponse
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.meetings.domain.policies.MeetingInvitePolicy
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingInvitesService
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import com.planify.planifyspring.main.features.meetings.routing.dto.MeetingInviteDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.get_invite.GetInviteResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.get_user_sent_invites.GetUserSentInvitesResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.reschedule_request.RescheduleRequestDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.reschedule_response.RescheduleAnswerRequestDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.send_invite.SendInviteRequestDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.send_invite.SendInviteResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/meetings/invites")
class MeetingInvitesController(
    val meetingInvitesService: MeetingInvitesService,
    val meetingsService: MeetingsService,
    val meetingInvitePolicy: MeetingInvitePolicy
) {
    @PostMapping("")
    fun sendInvite(
        @AuthenticationPrincipal authContext: AuthContext,
        @RequestBody body: SendInviteRequestDTO
    ): ResponseEntity<ApplicationResponse<SendInviteResponseDTO>> {
        val invite = meetingInvitesService.createInvite(
            meetingId = body.meetingId,
            senderId = authContext.user.id,
            targetId = body.targetId
        )

        return ResponseEntity.ok(
            SendInviteResponseDTO(
                invite = MeetingInviteDTO.fromEntity(invite)
            ).asSuccessApplicationResponse()
        )
    }

    @GetMapping("/my/sent")
    fun getUserSentInvites(
        @AuthenticationPrincipal authContext: AuthContext,
    ): ResponseEntity<ApplicationResponse<GetUserSentInvitesResponseDTO>> {
        val invites = meetingInvitesService.getUserSentInvites(userId = authContext.user.id)

        return ResponseEntity.ok(GetUserSentInvitesResponseDTO(
            invites = invites.map { MeetingInviteDTO.fromEntity(it) }
        ).asSuccessApplicationResponse())
    }

    @GetMapping("/{inviteUuid}")
    fun getInvite(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable inviteUuid: String
    ): ResponseEntity<ApplicationResponse<GetInviteResponseDTO>> {
        val invite = meetingInvitesService.getInvite(inviteUuid)
        meetingInvitePolicy.assertCanRead(  // FIXME: Business logic in controller?
            requesterId = authContext.user.id,
            invite = invite,
            isMeetingParticipant = meetingsService.isUserParticipant(authContext.user.id, invite.meetingId)
        )

        return ResponseEntity.ok(
            GetInviteResponseDTO(
                invite = MeetingInviteDTO.fromEntity(invite)
            ).asSuccessApplicationResponse()
        )
    }

    @PostMapping("/{inviteUuid}/accept")
    fun acceptInvite(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable inviteUuid: String
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        meetingInvitesService.acceptInvite(
            inviteUuid = inviteUuid,
            requesterId = authContext.user.id
        )

        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @PostMapping("/{inviteUuid}/reject")
    fun rejectInvite(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable inviteUuid: String
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        meetingInvitesService.rejectInvite(
            inviteUuid = inviteUuid,
            requesterId = authContext.user.id
        )

        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @PostMapping("/{inviteUuid}/reschedule/request")
    fun requestRescheduleInvite(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable inviteUuid: String,
        @RequestBody body: RescheduleRequestDTO
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        meetingInvitesService.requestRescheduleInvite(
            inviteUuid = inviteUuid,
            requesterId = authContext.user.id,
            rescheduleTo = body.rescheduleTo
        )

        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @PostMapping("/{inviteUuid}/reschedule/response")
    fun responseRescheduleInvite(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable inviteUuid: String,
        @RequestBody body: RescheduleAnswerRequestDTO
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        meetingInvitesService.responseRescheduleInvite(
            inviteUuid = inviteUuid,
            requesterId = authContext.user.id,
            shouldReschedule = body.shouldReschedule
        )

        return ResponseEntity.ok(ApplicationResponse.success())
    }
}
