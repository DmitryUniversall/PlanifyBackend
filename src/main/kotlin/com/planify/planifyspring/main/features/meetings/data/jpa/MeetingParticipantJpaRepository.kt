package com.planify.planifyspring.main.features.meetings.data.jpa

import com.planify.planifyspring.main.features.meetings.data.models.MeetingParticipantModel
import com.planify.planifyspring.main.features.meetings.data.records.DayMeetingsCountRecord
import com.planify.planifyspring.main.features.meetings.data.records.MeetingParticipantIdRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface MeetingParticipantJpaRepository : JpaRepository<MeetingParticipantModel, Long> {
    fun existsByUserIdAndMeeting_Id(userId: Long, meetingId: Long): Boolean

    @Query(  // TODO: Should i use JPQL here?
        """
            SELECT new com.planify.planifyspring.main.features.meetings.data.records.MeetingParticipantIdRecord(
                m,
                mp.userId
            )
            FROM MeetingModel m
            JOIN m.participants mp
            WHERE m.id IN (
                SELECT mp2.meeting.id
                FROM MeetingParticipantModel mp2
                WHERE mp2.userId = :userId
            )
            AND m.startsAt BETWEEN :startAt AND :endAt
            ORDER BY m.startsAt, m.id
        """
    )
    fun getUserDailyMeetingsWithParticipantIds(userId: Long, startAt: Instant, endAt: Instant): List<MeetingParticipantIdRecord>

    @Query(
        """
            SELECT new com.planify.planifyspring.main.features.meetings.data.records.DayMeetingsCountRecord(
                CAST(m.startsAt AS java.time.LocalDate), 
                COUNT(mp)
            )
            FROM MeetingParticipantModel mp
            JOIN mp.meeting m
            WHERE mp.userId = :userId
                AND m.startsAt BETWEEN :startAt AND :endAt
            GROUP BY CAST(m.startsAt AS java.time.LocalDate)
            ORDER BY CAST(m.startsAt AS java.time.LocalDate)
        """
    )
    fun getUserDailyMeetingsCount(
        userId: Long,
        startAt: Instant,
        endAt: Instant
    ): List<DayMeetingsCountRecord>
}
