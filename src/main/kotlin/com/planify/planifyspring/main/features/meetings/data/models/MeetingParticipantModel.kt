package com.planify.planifyspring.main.features.meetings.data.models

import com.planify.planifyspring.main.features.meetings.domain.entities.MeetingParticipant
import jakarta.persistence.*


@Entity
@Table(
    name = "meeting_participants",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_meeting_participant",
            columnNames = ["meeting_id", "user_id"]
        )
    ],
    indexes = [
        Index(name = "idx_meeting_participants_meeting_id", columnList = "meeting_id"),
        Index(name = "idx_meeting_participants_user_id", columnList = "user_id")
    ]
)
open class MeetingParticipantModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    val meeting: MeetingModel,

    @Column(name = "user_id", nullable = false)
    val userId: Long
) {
    fun toEntity(): MeetingParticipant {
        return MeetingParticipant(
            userId = userId,
            meetingId = this.meeting.id!!,
        )
    }
}
