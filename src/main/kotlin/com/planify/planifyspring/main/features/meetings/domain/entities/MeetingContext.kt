package com.planify.planifyspring.main.features.meetings.domain.entities

import com.planify.planifyspring.main.features.profiles.domain.entiries.Profile

data class MeetingContext(
    val participants: List<Profile>,  // TODO: Use MeetingParticipant instaed of profile
    val invites: List<MeetingInvite>,
    val meeting: Meeting
)
