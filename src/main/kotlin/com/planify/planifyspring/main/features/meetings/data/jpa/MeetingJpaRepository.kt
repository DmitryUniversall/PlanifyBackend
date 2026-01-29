package com.planify.planifyspring.main.features.meetings.data.jpa

import com.planify.planifyspring.main.features.meetings.data.models.MeetingModel
import com.planify.planifyspring.main.features.meetings.data.models.MeetingParticipantModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MeetingJpaRepository : JpaRepository<MeetingModel, Long> {
    @Query("""
        SELECT m
        FROM MeetingModel m
        JOIN MeetingParticipantModel mp
            ON mp.meetingId = m.id
        WHERE mp.userId = :userId
    """)
    fun findUserMeetings(userId: Long): List<MeetingModel>

    @Query("""
        SELECT u.id
        FROM UserModel u 
        JOIN MeetingParticipantModel mp
            on mp.userId = u.id
        where mp.meetingId = :meetingId
    """)
    fun findMeetingParticipantIds(meetingId: Long): List<Long>

    @Query("""
        SELECT u, p
        FROM UserModel u
        JOIN MeetingParticipantModel mp
            ON mp.userId = u.id
        JOIN ProfileModel p
            ON mp.userId = p.userId
        WHERE mp.meetingId = :meetingId
    """)
    fun findMeetingParticipantsWithFullInfo(meetingId: Long): List<>
}
