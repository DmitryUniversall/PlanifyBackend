package com.planify.planifyspring.main.features.meetings.domain.exceptions

import com.planify.planifyspring.core.exceptions.AccessDeniedAppError
import com.planify.planifyspring.core.exceptions.AlreadyExistsAppError
import com.planify.planifyspring.core.exceptions.ExpiredAppError
import com.planify.planifyspring.core.exceptions.NotFoundAppError
import com.planify.planifyspring.core.exceptions.UnprocessableEntityAppError

class MeetingNotFoundAppError(message: String = "Meeting was not found") : NotFoundAppError(message)
class InviteNotFoundAppError(message: String = "Invite was not found") : NotFoundAppError(message)

class NotMeetingOwnerAppError(message: String = "You are not the owner of this meeting") : AccessDeniedAppError(message)
class NotMeetingParticipantAppError(message: String = "You are not a participant of this meeting") : AccessDeniedAppError(message)
class NotInviteTargetAppError(message: String = "You are not the target of this invite") : AccessDeniedAppError(message)
class NotInviteSenderAppError(message: String = "You are not the sender of this invite") : AccessDeniedAppError(message)
class InviteAccessDeniedAppError(message: String = "You cannot access this invite") : AccessDeniedAppError(message)

class MeetingInPastAppError(message: String = "Cannot create meeting in the past") : UnprocessableEntityAppError(message)
class MeetingTimeConflictAppError(message: String = "User already has a meeting at this time interval") : UnprocessableEntityAppError(message)
class MeetingAlreadyStartedAppError(message: String = "Meeting has already started") : UnprocessableEntityAppError(message)
class RescheduleToPastAppError(message: String = "Cannot reschedule meeting to the past") : UnprocessableEntityAppError(message)

class TargetAlreadyInvitedAppError(message: String = "Target already has an invite to this meeting") : AlreadyExistsAppError(message)
class TargetAlreadyParticipantAppError(message: String = "Target is already a participant of this meeting") : AlreadyExistsAppError(message)

class InviteAlreadyRepliedAppError(message: String = "Invite has already been replied") : UnprocessableEntityAppError(message)
class InviteExpiredAppError(message: String = "Invite is expired") : ExpiredAppError(message)
class RescheduleNotRequestedAppError(message: String = "Reschedule is not requested for this invite") : UnprocessableEntityAppError(message)
