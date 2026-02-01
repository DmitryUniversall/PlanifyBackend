package com.planify.planifyspring.main.features.meetings.routing

import com.planify.planifyspring.core.utils.atEndOfDayInstant
import com.planify.planifyspring.core.utils.atStartOfDayInstant
import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessApplicationResponse
import com.planify.planifyspring.main.exceptions.generics.NotFoundHttpException
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingPatchSchema
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingInvitesService
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import com.planify.planifyspring.main.features.meetings.routing.dto.MeetingContextDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.MeetingDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.MeetingInviteDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.create_meeting.CreateMeetingRequestDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.create_meeting.CreateMeetingResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.get_meeting.GetMeetingResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.get_my_meetings.GetMyMeetingsResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.get_my_meetings_short.GetMyMeetingsShortResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.patch_meeting.PatchMeetingRequestDTO
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
    val profileService: ProfilesService
) {
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

        body.inviteUserIds?.let { userIds ->
            userIds.forEach {
                meetingInvitesService.createInvite(
                    meetingId = meeting.id,
                    senderId = authContext.user.id,
                    targetId = it
                )
            }
        }

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
        val meeting = meetingsService.getMeetingById(
            meetingId = meetingId,
            requesterId = authContext.user.id
        ) ?: throw NotFoundHttpException("Meeting was not found")

        return ResponseEntity.ok(
            GetMeetingResponseDTO(

                meeting = MeetingDTO.fromEntity(meeting)
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
            meetingId = meetingId,
            updaterId = authContext.user.id,
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
                meetings = meetings.mapValues { (_, meetings) ->
                    meetings.map { (meeting, participantIds) ->
                        val invites = meetingInvitesService.getMeetingInvites(
                            meetingId = meeting.id,
                            requesterId = authContext.user.id
                        ).map { MeetingInviteDTO.fromEntity(it) }

                        val participantProfiles = participantIds.map {
                            ProfileDTO.fromEntity(profileService.getProfileById(it))  // TODO: Optimise it via db query
                        }

                        val meeting = MeetingDTO.fromEntity(meeting)
                        MeetingContextDTO(
                            participantProfiles = participantProfiles,
                            invites = invites,
                            meeting = meeting
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
