package com.planify.planifyspring.main.features.meetings.data.models

import com.planify.planifyspring.main.features.meetings.domain.entities.Meeting
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "meetings")
open class MeetingModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val ownerId: Long,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val startsAt: Instant,

    @Column(nullable = false)
    val duration: Int,

    @Column(nullable = true)
    val description: String,

    @Column(nullable = true)
    val location: String,

    @OneToMany(
        mappedBy = "meeting",
        fetch = FetchType.LAZY,
        cascade = [],
        orphanRemoval = false
    )
    val participants: MutableSet<MeetingParticipantModel> = mutableSetOf()
) {
    fun toEntity(): Meeting = Meeting(
        id = id!!,
        ownerId = ownerId,
        name = name,
        startsAt = startsAt,
        duration = duration,
        description = description,
        location = location
    )
}
