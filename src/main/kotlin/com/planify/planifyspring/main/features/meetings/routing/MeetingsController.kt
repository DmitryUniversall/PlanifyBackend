package com.planify.planifyspring.main.features.meetings.routing

import com.planify.planifyspring.core.utils.atEndOfDayInstant
import com.planify.planifyspring.core.utils.atStartOfDayInstant
import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessApplicationResponse
import com.planify.planifyspring.main.exceptions.generics.NotFoundHttpException
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.meetings.domain.policies.MeetingPolicy
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingPatchSchema
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingInvitesService
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import com.planify.planifyspring.main.features.meetings.routing.dto.MeetingContextDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.MeetingDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.MeetingInviteDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.create_meeting.CreateMeetingRequestDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.create_meeting.CreateMeetingResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.get_meeting.GetMeetingResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.get_meeting_participant.GetMeetingParticipantsResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.get_my_meetings.GetMyMeetingsResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.get_my_meetings_short.GetMyMeetingsShortResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.patch_meeting.PatchMeetingRequestDTO
import com.planify.planifyspring.main.features.meetings.routing.get_meeting_with_context.GetMeetingWithContextResponseDTO
import com.planify.planifyspring.main.features.profiles.domain.services.ProfilesService
import com.planify.planifyspring.main.features.profiles.routing.dto.ProfileDTO
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/meetings")
class MeetingsController(
    val meetingsService: MeetingsService,
    val meetingInvitesService: MeetingInvitesService,
    val profilesService: ProfilesService,
    val meetingPolicy: MeetingPolicy
) {
    private fun profileDtoOf(userId: Long): ProfileDTO =
        ProfileDTO.fromEntity(
            profilesService.getProfileById(userId) ?: throw NotFoundHttpException("Profile for this user $userId was not found")
        )

    @PostMapping("")
    fun createMeeting(
        @AuthenticationPrincipal authContext: AuthContext,
        @RequestBody body: CreateMeetingRequestDTO
    ): ResponseEntity<ApplicationResponse<CreateMeetingResponseDTO>> {
        val meeting = meetingsService.createMeeting(
            ownerId = authContext.user.id,
            name = body.name,
            description = body.description,
            location = body.location,
            startsAt = body.startsAt,
            duration = body.duration
        )

        return ResponseEntity.ok(
            CreateMeetingResponseDTO(
                meeting = MeetingDTO.fromEntity(meeting)
            ).asSuccessApplicationResponse()
        )
    }

    @GetMapping("/{meetingId}")
    fun getMeeting(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable meetingId: Long,
    ): ResponseEntity<ApplicationResponse<GetMeetingResponseDTO>> {
        val meeting = meetingsService.getMeetingById(meetingId)
        meetingPolicy.assertCanView(
            requesterId = authContext.user.id,
            meeting = meeting,
            isParticipant = meetingsService.isUserParticipant(authContext.user.id, meetingId)
        )

        return ResponseEntity.ok(
            GetMeetingResponseDTO(
                meeting = MeetingDTO.fromEntity(meeting)
            ).asSuccessApplicationResponse()
        )
    }

    @GetMapping("/{meetingId}/context")
    fun getMeetingWithContext(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable meetingId: Long,
    ): ResponseEntity<ApplicationResponse<GetMeetingWithContextResponseDTO>> {
        val meetingWithParticipantIds = meetingsService.getMeetingWithParticipantIds(meetingId)
        meetingPolicy.assertCanView(
            requesterId = authContext.user.id,
            meeting = meetingWithParticipantIds.meeting,
            isParticipant = meetingWithParticipantIds.participantIds.contains(authContext.user.id)
        )

        val invites = meetingInvitesService.getMeetingInvites(meetingWithParticipantIds.meeting.id)
            .map { MeetingInviteDTO.fromEntity(it) }

        val participantProfiles = meetingWithParticipantIds.participantIds.map { profileDtoOf(it) }  // TODO: Optimise via batch db query

        val invitedUserProfiles = invites.map { profileDtoOf(it.targetId) }

        val meeting = MeetingDTO.fromEntity(meetingWithParticipantIds.meeting)

        return ResponseEntity.ok(
            GetMeetingWithContextResponseDTO(
                meetingContext = MeetingContextDTO(
                    participantProfiles = participantProfiles,
                    invites = invites,
                    meeting = meeting,
                    invitedUserProfiles = invitedUserProfiles
                )
            ).asSuccessApplicationResponse()
        )
    }

    @GetMapping("/{meetingId}/participants")
    fun getMeetingParticipants(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable meetingId: Long,
    ): ResponseEntity<ApplicationResponse<GetMeetingParticipantsResponseDTO>> {
        val info = meetingsService.getMeetingWithParticipantIds(meetingId)
        meetingPolicy.assertCanView(
            requesterId = authContext.user.id,
            meeting = info.meeting,
            isParticipant = info.participantIds.contains(authContext.user.id)
        )

        return ResponseEntity.ok(
            GetMeetingParticipantsResponseDTO(
                meeting = MeetingDTO.fromEntity(info.meeting),
                participantIds = info.participantIds
            ).asSuccessApplicationResponse()
        )
    }

    @PatchMapping("/{meetingId}")
    fun patchMeeting(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable meetingId: Long,
        @RequestBody body: PatchMeetingRequestDTO,
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        meetingsService.patchMeeting(
            requesterId = authContext.user.id,
            meetingId = meetingId,
            patch = MeetingPatchSchema(
                name = body.name,
                description = body.description,
                location = body.location,
                startsAt = body.startsAt,
                duration = body.duration,
            )
        )

        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @GetMapping("/my")
    fun getMyMeetings(
        @AuthenticationPrincipal authContext: AuthContext,
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") dateStart: LocalDate,
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") dateEnd: LocalDate
    ): ResponseEntity<ApplicationResponse<GetMyMeetingsResponseDTO>> {
        val meetings = meetingsService.getUserDailyMeetingsWithParticipantIds(
            userId = authContext.user.id,
            startAt = dateStart.atStartOfDayInstant(),
            endAt = dateEnd.atEndOfDayInstant()
        )

        return ResponseEntity.ok(
            GetMyMeetingsResponseDTO(
                meetings = meetings.mapValues { (_, dayMeetings) ->
                    dayMeetings.map { (meeting, participantIds) ->
                        val invites = meetingInvitesService.getMeetingInvites(meeting.id)
                            .map { MeetingInviteDTO.fromEntity(it) }

                        val participantProfiles = participantIds.map { profileDtoOf(it) }  // TODO: Optimise via batch db query
                        val invitedUserProfiles = invites.map { profileDtoOf(it.targetId) }

                        MeetingContextDTO(
                            participantProfiles = participantProfiles,
                            invites = invites,
                            meeting = MeetingDTO.fromEntity(meeting),
                            invitedUserProfiles = invitedUserProfiles
                        )
                    }
                }
            ).asSuccessApplicationResponse()
        )
    }

    @GetMapping("/my/short")
    fun getMyMeetingsShort(
        @AuthenticationPrincipal authContext: AuthContext,
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") dateStart: LocalDate,
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") dateEnd: LocalDate
    ): ResponseEntity<ApplicationResponse<GetMyMeetingsShortResponseDTO>> {
        val meetings = meetingsService.getUserDailyMeetingsShort(
            userId = authContext.user.id,
            startAt = dateStart.atStartOfDayInstant(),
            endAt = dateEnd.atEndOfDayInstant()
        )

        return ResponseEntity.ok(
            GetMyMeetingsShortResponseDTO(
                meetings = meetings
            ).asSuccessApplicationResponse()
        )
    }
}
