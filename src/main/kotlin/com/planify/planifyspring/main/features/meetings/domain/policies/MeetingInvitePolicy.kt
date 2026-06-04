package com.planify.planifyspring.main.features.meetings.domain.policies

import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInvite
import com.planify.planifyspring.main.features.meetings.domain.exceptions.InviteAccessDeniedAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.NotInviteSenderAppError
import com.planify.planifyspring.main.features.meetings.domain.exceptions.NotInviteTargetAppError
import org.springframework.stereotype.Component

@Component
class MeetingInvitePolicy {
    fun assertCanRead(requesterId: Long, invite: MeetingInvite, isMeetingParticipant: Boolean) {
        if (requesterId == invite.senderId || requesterId == invite.targetId || isMeetingParticipant) return
        throw InviteAccessDeniedAppError()
    }

    fun assertIsTarget(requesterId: Long, invite: MeetingInvite) {
        if (invite.targetId != requesterId) throw NotInviteTargetAppError()
    }

    fun assertIsSender(requesterId: Long, invite: MeetingInvite) {
        if (invite.senderId != requesterId) throw NotInviteSenderAppError()
    }
}
