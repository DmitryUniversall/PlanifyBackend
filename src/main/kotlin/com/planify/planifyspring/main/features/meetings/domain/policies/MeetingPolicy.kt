package com.planify.planifyspring.main.features.meetings.domain.policies

import com.planify.planifyspring.main.features.meetings.domain.entities.Meeting
import com.planify.planifyspring.main.features.meetings.domain.exceptions.MeetingAccessDeniedAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.NotMeetingOwnerAppError
import org.springframework.stereotype.Component

@Component
class MeetingPolicy {
    fun assertIsOwner(requesterId: Long, meeting: Meeting) {
        if (meeting.ownerId != requesterId) throw NotMeetingOwnerAppError()
    }

    fun assertCanView(
        requesterId: Long,
        meeting: Meeting,
        isParticipant: Boolean,
        isInvited: Boolean = false
    ) {
        if (meeting.ownerId == requesterId) return
        if (isParticipant || isInvited) return

        throw MeetingAccessDeniedAppError()
    }
}
