package com.planify.planifyspring.main.features.meetings.routing

import com.planify.planifyspring.core.utils.atStartOfDay
import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessResponse
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingPatchSchema
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import com.planify.planifyspring.main.features.meetings.routing.dto.CreateMeetingRequestDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.GetMyMeetingsResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.GetMyMeetingsShortResponseDTO
import com.planify.planifyspring.main.features.meetings.routing.dto.PatchMeetingRequestDTO
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/meetings")
class MeetingsController(
    val meetingsService: MeetingsService
) {
    @PostMapping("")
    fun createMeeting(
        @AuthenticationPrincipal authContext: AuthContext,
        @RequestBody body: CreateMeetingRequestDTO
    ) {
        meetingsService.createMeeting(
            ownerId = authContext.user.id,
            name = body.name,
            description = body.description,
            location = body.location,
            startsAt = body.startsAt,
            duration = body.duration,
            inviteUserIds = body.inviteUserIds
        )
    }

    @PatchMapping("/{meetingId}")
    fun patchMeeting(
        @AuthenticationPrincipal authContext: AuthContext,
        @PathVariable meetingId: Long,
        @RequestBody body: PatchMeetingRequestDTO,
    ) {
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
    }

    @GetMapping("/my")
    fun getMyMeetings(
        @AuthenticationPrincipal authContext: AuthContext,
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") dateStart: Instant?,
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") dateEnd: Instant?
    ): ResponseEntity<ApplicationResponse<GetMyMeetingsResponseDTO>> {
//        val meetings = meetingsService.getUserMeetings(
//            userId = authContext.user.id,
//            dateStart = dateStart,
//            endDate = dateEnd
//        )
//
//        return ResponseEntity.ok(
//            GetMyMeetingsResponseDTO(
//                meetings = meetings.mapValues { (_, meetings) ->
//                    meetings.map { MeetingDTO.fromEntity(it) }
//                }
//            ).asSuccessResponse()
//        )
        TODO()
    }

    @GetMapping("/my/short")
    fun getMyMeetingsShort(
        @AuthenticationPrincipal authContext: AuthContext,
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") dateStart: Instant,
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") dateEnd: Instant
    ): ResponseEntity<ApplicationResponse<GetMyMeetingsShortResponseDTO>> {
        val meetings = meetingsService.getUserDailyMeetingsShort(
            userId = authContext.user.id,
            startAt = dateStart.atStartOfDay(),
            endAt = dateEnd.atStartOfDay()
        )

        return ResponseEntity.ok(
            GetMyMeetingsShortResponseDTO(
                meetings = meetings
            ).asSuccessResponse()
        )
    }
}
