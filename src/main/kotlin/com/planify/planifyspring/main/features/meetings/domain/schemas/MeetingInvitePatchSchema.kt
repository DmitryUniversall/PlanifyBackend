package com.planify.planifyspring.main.features.meetings.domain.schemas

import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInviteStatus

class MeetingInvitePatchSchema(
    val status: MeetingInviteStatus? = null,
    val statusData: Any? = null
)
