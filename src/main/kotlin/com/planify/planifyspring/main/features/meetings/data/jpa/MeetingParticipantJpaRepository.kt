package com.planify.planifyspring.main.features.meetings.data.jpa

import com.planify.planifyspring.main.features.meetings.data.models.MeetingParticipantModel
import com.planify.planifyspring.main.features.meetings.data.records.MeetingParticipantIdRecord
import com.planify.planifyspring.main.features.meetings.data.records.DayMeetingsCountRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface MeetingParticipantJpaRepository : JpaRepository<MeetingParticipantModel, Long> {
    @Query(
        """
            SELECT 
                m as meeting,
                mp.user_id as participantId
            FROM
                meeting_participants mp
            JOIN 
                meetings m ON m.id = mp.meeting_id
            WHERE 
                mp.meeting_id IN (
                    SELECT meeting_id
                    FROM meeting_participants
                    WHERE user_id = :userId
                )
                AND m.starts_at BETWEEN :startAt AND :endAt
            ORDER BY
                m.starts_at, m.id
        """, nativeQuery = true
    )
    fun getUserDailyMeetingsWithParticipantIds(userId: Long, startAt: Instant, endAt: Instant): List<MeetingParticipantIdRecord>

    @Query(
        """
            SELECT
                DATE(m.starts_at) as date,
                COUNT(*) as count
            FROM meeting_participants mp 
            JOIN meetings m on mp.meeting_id = m.id
            WHERE mp.user_id = :userId AND m.starts_at BETWEEN :startAt AND :endAt
            GROUP BY DATE(m.starts_at)
            ORDER BY DATE(m.starts_at)
        """, nativeQuery = true
    )
    fun getUserDailyMeetingsCount(userId: Long, startAt: Instant, endAt: Instant): List<DayMeetingsCountRecord>
}
