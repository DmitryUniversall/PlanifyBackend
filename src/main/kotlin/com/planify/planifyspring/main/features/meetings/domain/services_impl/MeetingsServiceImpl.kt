package com.planify.planifyspring.main.features.meetings.domain.services_impl

import com.planify.planifyspring.core.utils.validateRange
import com.planify.planifyspring.main.exceptions.generics.BadRequestHttpException
import com.planify.planifyspring.main.features.meetings.domain.entities.Meeting
import com.planify.planifyspring.main.features.meetings.domain.services.MeetingsService
import org.springframework.stereotype.Service
import java.util.Date

@Service
class MeetingsServiceImpl : MeetingsService {
    override fun getUserMeetings(
        userId: Long,
        dateStart: Date?,
        endDate: Date?
    ): List<Meeting> {
        if(!validateRange(dateStart, endDate)) throw BadRequestHttpException("Invalid date range")
        TODO("Not yet implemented")
    }

    override fun createMeeting(
        ownerId: Long,
        name: String,
        description: String,
        location: String,
        startsAt: Date,
        duration: Int
    ): Meeting {
        TODO("Not yet implemented")
    }
}
