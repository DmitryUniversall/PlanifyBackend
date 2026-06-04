package com.planify.planifyspring.core.exceptions

open class AlreadyExistsAppError(message: String) : ApplicationException(message)
open class NotFoundAppError(message: String) : ApplicationException(message)
open class AlreadyInUseAppError(message: String) : ApplicationException(message)
open class InvalidArgumentAppError(message: String) : ApplicationException(message)

open class AccessDeniedAppError(message: String) : ApplicationException(message)
open class ExpiredAppError(message: String) : ApplicationException(message)
open class UnprocessableEntityAppError(message: String) : ApplicationException(message)
