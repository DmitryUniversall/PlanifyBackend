package com.planify.planifyspring.main.features.meetings.domain.entities

import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile

data class MeetingInviteContext(
    val invite: MeetingInvite,
    val meeting: Meeting,
    val senderProfile: Profile,
    val targetProfile: Profile
)
