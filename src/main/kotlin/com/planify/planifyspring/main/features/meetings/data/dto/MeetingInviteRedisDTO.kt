package com.planify.planifyspring.main.features.meetings.data.dto

import com.planify.planifyspring.main.common.utils.ObjectMapHelper
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInvite
import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingInviteStatus
import java.time.Instant

data class MeetingInviteRedisDTO(
    val uuid: String,
    val meetingId: Long,
    val senderId: Long,
    val targetId: Long,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val expiresAt: String,
    val statusData: String? = null
) {
    companion object {
        fun fromEntity(entity: MeetingInvite, mapper: ObjectMapHelper) = MeetingInviteRedisDTO(
            uuid = entity.uuid,
            meetingId = entity.meetingId,
            senderId = entity.senderId,
            targetId = entity.targetId,
            status = entity.status.name,
            createdAt = entity.createdAt.toString(),
            updatedAt = entity.updatedAt.toString(),
            expiresAt = entity.expiresAt.toString(),
            statusData = entity.statusData?.let { mapper.convertToString(it) }
        )
    }

    fun toEntity(mapper: ObjectMapHelper): MeetingInvite = MeetingInvite(
        uuid = uuid,
        meetingId = meetingId,
        senderId = senderId,
        targetId = targetId,
        status = MeetingInviteStatus.valueOf(status),
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
        expiresAt = Instant.parse(expiresAt),
        statusData = statusData?.let { mapper.convertFromString(it, Any::class.java) }
    )
}