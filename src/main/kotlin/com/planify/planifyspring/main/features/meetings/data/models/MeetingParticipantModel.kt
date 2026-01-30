package com.planify.planifyspring.main.features.meetings.data.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable


data class MeetingParticipantId(
    val meetingId: Long,
    val userId: Long
) : Serializable


@Entity
@Table(name = "meeting_participants")
@IdClass(MeetingParticipantId::class)
open class MeetingParticipantModel(
    @Id
    @Column(nullable = false)
    val userId: Long,

    @Id
    @Column(nullable = false)
    val meetingId: Long
)
