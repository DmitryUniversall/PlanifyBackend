package com.planify.planifyspring.main.features.meetings.domain.entities

import java.time.Instant

data class MeetingInvite(
    val fromUserId: Long,
    val toUserId: Long,
    val createdAt: Instant,
    val status: MeetingInviteStatus
)
