package com.planify.planifyspring.main.features.meetings.routing.dto.get_sent_invites_with_context

import com.planify.planifyspring.main.features.meetings.routing.dto.MeetingInviteContextDTO

data class GetUserSentInvitesWithContextResponseDTO(
    val invites: List<MeetingInviteContextDTO>
)
