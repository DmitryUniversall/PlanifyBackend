package com.planify.planifyspring.main.features.meetings.domain.schemas

import java.time.Instant

data class MeetingActionMeetingUpdatedSchema(
    val updaterId: Long,
    val meetingId: Long,
    val patch: MeetingPatchSchema,
    val updatedAt: Instant,
)
