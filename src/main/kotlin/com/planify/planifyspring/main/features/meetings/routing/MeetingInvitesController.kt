package com.planify.planifyspring.main.features.meetings.routing

import com.planify.planifyspring.core.utils.asUTCInstant
import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessApplicationResponse
import com.planify.planifyspring.main.exceptions.generics.NotFoundHttpException
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingInvitesService
import com.planify.planifyspring.main.features.meetings.routing.dto.MeetingInviteDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.get_invite.GetInviteResponseDTO
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
    val meetingInviteService: MeetingInvitesService
) {
    @PostMapping("")
    fun sendInvite(
        @AuthenticationPrincipal authContext: AuthContext,
        @RequestBody body: SendInviteRequestDTO
    ): ResponseEntity<ApplicationResponse<SendInviteResponseDTO>> {
        val invite = meetingInviteService.createInvite(
            meetingId = body.meetingId,
            senderId = authContext.user.id,
            targetId = body.targetUserId
        )

        return ResponseEntity.ok(
            SendInviteResponseDTO(
                invite = MeetingInviteDTO.fromEntity(invite)
            ).asSuccessApplicationResponse()
        )
    }

    @GetMapping("/{inviteUuid}")
    fun getInvite(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable inviteUuid: String
    ): ResponseEntity<ApplicationResponse<GetInviteResponseDTO>> {
        val invite = meetingInviteService.getInvite(
            inviteUuid = inviteUuid,
            requesterId = authContext.user.id
        ) ?: throw NotFoundHttpException("Invite was not found")

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
        meetingInviteService.acceptInvite(
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
        meetingInviteService.rejectInvite(
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
        meetingInviteService.requestRescheduleInvite(
            inviteUuid = inviteUuid,
            requesterId = authContext.user.id,
            rescheduleTo = body.rescheduleTo.asUTCInstant()
        )

        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @PostMapping("/{inviteUuid}/reschedule/response")
    fun responseRescheduleInvite(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable inviteUuid: String,
        @RequestBody body: RescheduleAnswerRequestDTO
    ): ResponseEntity<ApplicationResponse<Nothing>> {

        meetingInviteService.responseRescheduleInvite(
            inviteUuid = inviteUuid,
            requesterId = authContext.user.id,
            shouldReschedule = body.shouldReschedule
        )

        return ResponseEntity.ok(ApplicationResponse.success())
    }
}
