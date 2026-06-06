package com.planify.planifyspring.main.features.meetings.routing.dto.get_user_sent_invites

import com.planify.planifyspring.main.features.meetings.routing.dto.MeetingInviteDTO

data class GetUserSentInvitesResponseDTO(
    val invites: List<MeetingInviteDTO>
)
