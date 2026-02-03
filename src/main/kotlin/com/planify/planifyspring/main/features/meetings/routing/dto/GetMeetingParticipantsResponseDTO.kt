package com.planify.planifyspring.main.features.meetings.routing.dto

data class GetMeetingParticipantsResponseDTO(
    val meeting: MeetingDTO,
    val participantIds: List<Long>
)
