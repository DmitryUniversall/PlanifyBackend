package com.planify.planifyspring.main.features.meetings.routing.dto

import com.planify.planifyspring.main.features.profiles.routing.dto.ProfileDTO

class MeetingInviteContextDTO(
    val invite: MeetingInviteDTO,
    val meeting: MeetingDTO,
    val senderProfile: ProfileDTO,
    val targetProfile: ProfileDTO
)
