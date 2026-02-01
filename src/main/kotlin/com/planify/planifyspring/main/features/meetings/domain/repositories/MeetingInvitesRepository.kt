package com.planify.planifyspring.main.features.meetings.domain.repositories

import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInvite
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingInviteParchSchema

interface MeetingInvitesRepository {
    fun createInvite(meetingId: Long, senderId: Long, targetId: Long): MeetingInvite

    fun getInvite(uuid: String): MeetingInvite?

    fun updateInvite(inviteUuid: String, patch: MeetingInviteParchSchema)

    fun getMeetingInvites(meetingId: Long): List<MeetingInvite>
}
