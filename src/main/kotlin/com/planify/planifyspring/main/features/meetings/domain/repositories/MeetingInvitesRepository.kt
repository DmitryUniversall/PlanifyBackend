package com.planify.planifyspring.main.features.meetings.domain.repositories

import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInvite
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingInvitePatchSchema
import java.time.Instant

interface MeetingInvitesRepository {
    fun createInvite(meetingId: Long, senderId: Long, targetId: Long, expiresAt: Instant): MeetingInvite

    fun getInvite(uuid: String): MeetingInvite?

    fun updateInvite(inviteUuid: String, patch: MeetingInvitePatchSchema)

    fun getMeetingInvites(meetingId: Long): List<MeetingInvite>

    fun hasActiveInvite(meetingId: Long, userId: Long): Boolean

    fun getUserSentInvites(userId: Long): List<MeetingInvite>

    fun deleteInvite(uuid: String)

    fun getManyInvites(uuids: Collection<String>): List<MeetingInvite>
}
