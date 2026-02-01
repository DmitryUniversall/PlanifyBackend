package com.planify.planifyspring.main.features.meetings.data.repositories_impl

import com.planify.planifyspring.main.common.utils.redis.RedisHelper
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInvite
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInviteStatus
import com.planify.planifyspring.main.features.meetings.domain.repositories.MeetingInvitesRepository
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingInviteParchSchema
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class MeetingInvitesRepositoryImpl(
    val redisHelper: RedisHelper
) : MeetingInvitesRepository {
    private fun generateInviteUuid(): String {
        return UUID.randomUUID().toString()
    }

    private fun getInviteKey(uuid: String): String {
        return "meetings:invites:$uuid"
    }

    private fun getMeetingInvitesKey(meetingId: Long): String {
        return "meetings:$meetingId:invites"
    }

    override fun createInvite(meetingId: Long, senderId: Long, targetId: Long): MeetingInvite {
        val invite = MeetingInvite(
            uuid = generateInviteUuid(),
            meetingId = meetingId,
            senderId = senderId,
            targetId = targetId,
            status = MeetingInviteStatus.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        redisHelper.hset(getInviteKey(invite.uuid), invite)
        redisHelper.addToSet(getMeetingInvitesKey(meetingId), invite.uuid)  // Save for faster lookup
        return invite
    }

    override fun getInvite(uuid: String): MeetingInvite? {
        return redisHelper.hget(getInviteKey(uuid), MeetingInvite::class.java)
    }

    override fun updateInvite(inviteUuid: String, patch: MeetingInviteParchSchema) {
        val key = getInviteKey(inviteUuid)

        patch.status?.let { redisHelper.hsetField(key, "status", patch.status) }
        patch.statusData?.let { redisHelper.hsetField(key, "statusData", patch.statusData) }

        redisHelper.hsetField(key, "updatedAt", Instant.now())
    }

    override fun getMeetingInvites(meetingId: Long): List<MeetingInvite> {
        val ids = redisHelper.getSet(getMeetingInvitesKey(meetingId), String::class.java)
        return ids.map { getInvite(it)!! }
    }
}
