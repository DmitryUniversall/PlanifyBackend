package com.planify.planifyspring.main.features.meetings.data.repositories_impl

import com.planify.planifyspring.core.utils.before
import com.planify.planifyspring.main.common.utils.ObjectMapHelper
import com.planify.planifyspring.main.common.utils.redis.RedisHelper
import com.planify.planifyspring.main.features.meetings.data.dto.MeetingInviteRedisDTO
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInvite
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInviteStatus
import com.planify.planifyspring.main.features.meetings.domain.repositories.MeetingInvitesRepository
import com.planify.planifyspring.main.features.meetings.domain.schemas.MeetingInvitePatchSchema
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class MeetingInvitesRepositoryImpl(
    private val redisHelper: RedisHelper,
    private val objectMapHelper: ObjectMapHelper
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

    private fun getUserSentInvitesKey(userId: Long) = "users:$userId:sent_invites"

    private fun getInvitedUsersKey(meetingId: Long) = "meetings:$meetingId:invited_users"

    private fun checkInviteStatus(invite: MeetingInvite): MeetingInvite {
        return if (invite.status == MeetingInviteStatus.PENDING && invite.expiresAt before Instant.now())
            invite.copy(status = MeetingInviteStatus.EXPIRED)
        else invite
    }

    private fun getInvitePostProcessing(invite: MeetingInvite): MeetingInvite {
        return checkInviteStatus(invite)
    }

    override fun createInvite(meetingId: Long, senderId: Long, targetId: Long, expiresAt: Instant): MeetingInvite {
        val invite = MeetingInvite(
            uuid = generateInviteUuid(),
            meetingId = meetingId,
            senderId = senderId,
            targetId = targetId,
            status = MeetingInviteStatus.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiresAt = expiresAt
        )

        val inviteDTO = MeetingInviteRedisDTO.fromEntity(invite, objectMapHelper)

        // Actual data
        redisHelper.hsetObject(getInviteKey(invite.uuid), inviteDTO)

        // References for faster lookup
        val score = invite.createdAt.toEpochMilli().toDouble()
        redisHelper.zadd(getMeetingInvitesKey(meetingId), score, invite.uuid)
        redisHelper.zadd(getUserSentInvitesKey(senderId), score, invite.uuid)
        redisHelper.hsetField(getInvitedUsersKey(meetingId), targetId.toString(), invite.uuid)

        return invite
    }

    override fun deleteInvite(uuid: String) {
        val inviteDTO = redisHelper.hgetObject(getInviteKey(uuid), MeetingInviteRedisDTO::class.java) ?: return

        // Delete references
        redisHelper.zrem(getMeetingInvitesKey(inviteDTO.meetingId), uuid)
        redisHelper.zrem(getUserSentInvitesKey(inviteDTO.senderId), uuid)

        val invitedKey = getInvitedUsersKey(inviteDTO.meetingId)
        val field = inviteDTO.targetId.toString()
        if (redisHelper.hgetField(invitedKey, field, String::class.java) == uuid) redisHelper.hdelFields(invitedKey, field)

        // Delete actual data
        redisHelper.del(getInviteKey(uuid))
    }

    override fun getInvite(uuid: String): MeetingInvite? {
        return redisHelper.hgetObject(getInviteKey(uuid), MeetingInviteRedisDTO::class.java)?.let { getInvitePostProcessing(it.toEntity(objectMapHelper)) }
    }

    override fun getManyInvites(uuids: Collection<String>): List<MeetingInvite> {
        val invites = redisHelper.hgetObjects(uuids.map { getInviteKey(it) }, MeetingInviteRedisDTO::class.java)
        return invites.map { getInvitePostProcessing(it.toEntity(objectMapHelper)) }
    }

    override fun updateInvite(inviteUuid: String, patch: MeetingInvitePatchSchema) {
        val key = getInviteKey(inviteUuid)

        val dto = redisHelper.hgetObject(key, MeetingInviteRedisDTO::class.java) ?: return

        redisHelper.hsetObject(
            key, dto.copy(
                status = patch.status?.name ?: dto.status,
                statusData = patch.statusData?.let { objectMapHelper.convertToString(it) } ?: dto.statusData,
                updatedAt = Instant.now().toString()
            ))
    }

    override fun getMeetingInvites(meetingId: Long): List<MeetingInvite> {
        return getManyInvites(
            uuids = redisHelper.zrevrange(
                key = getMeetingInvitesKey(meetingId),
                start = 0,
                end = -1,
                clazz = String::class.java
            )
        )
    }

    override fun getUserSentInvites(userId: Long): List<MeetingInvite> {
        return getManyInvites(
            uuids = redisHelper.zrevrange(
                key = getUserSentInvitesKey(userId),
                start = 0,
                end = -1,
                clazz = String::class.java
            )
        )
    }

    override fun hasActiveInvite(meetingId: Long, userId: Long): Boolean {
        val key = getInvitedUsersKey(meetingId)

        val inviteUUID = redisHelper.hgetField(key = key, field = userId.toString(), clazz = String::class.java) ?: return false
        val inviteDTO = getInvite(inviteUUID) ?: return false

        return when (inviteDTO.status) {
            MeetingInviteStatus.PENDING,
            MeetingInviteStatus.RESCHEDULE_REQUESTED -> true

            MeetingInviteStatus.ACCEPTED,
            MeetingInviteStatus.REJECTED,
            MeetingInviteStatus.EXPIRED -> false
        }
    }
}
