package com.planify.planifyspring.main.features.meetings.routing.dto

import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingContext
import com.planify.planifyspring.main.features.profiles.routing.dto.ProfileDTO

data class MeetingContextDTO(
    val participants: List<ProfileDTO>,
    val invites: List<MeetingInviteDTO>,
    val meeting: MeetingDTO
) {
    companion object {
        fun fromEntity(entity: MeetingContext): MeetingContextDTO = MeetingContextDTO(
            participants = entity.participants.map { ProfileDTO.fromEntity(it) },
            invites = entity.invites.map { MeetingInviteDTO.fromEntity(it) },
            meeting = MeetingDTO.fromEntity(entity.meeting)
        )
    }
}
