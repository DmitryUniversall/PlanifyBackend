package com.planify.planifyspring.main.features.meetings.data.models

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "meetings")
open class MeetingModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = false)
    val ownerId: Long,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val startsAt: Date,

    @Column(nullable = false)
    val duration: Int,

    @Column(nullable = true)
    val description: String,

    @Column(nullable = true)
    val location: String
)
