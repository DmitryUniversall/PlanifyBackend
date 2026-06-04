package com.planify.planifyspring.main.exceptions.generics

import com.planify.planifyspring.main.exceptions.ApplicationHttpException
import org.springframework.http.HttpStatus

open class InternalServerErrorHttpException(
    message: String?,
    appCode: Int = 2000
) : ApplicationHttpException(
    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    appCode = appCode,
    message = message
)

open class AlreadyExistsHttpException(
    message: String?,
    appCode: Int = 2001,
) : ApplicationHttpException(
    httpStatus = HttpStatus.CONFLICT,
    appCode = appCode,
    message = message
)

open class BadRequestHttpException(
    message: String?,
    appCode: Int = 2005
) : ApplicationHttpException(
    httpStatus = HttpStatus.BAD_REQUEST,
    appCode = appCode,
    message = message
)

open class NotFoundHttpException(
    message: String?,
    appCode: Int = 2002
) : ApplicationHttpException(
    httpStatus = HttpStatus.NOT_FOUND,
    appCode = appCode,
    message = message
)

open class UnauthorizedHttpException(
    message: String?,
    appCode: Int = 3002
) : ApplicationHttpException(
    httpStatus = HttpStatus.UNAUTHORIZED,
    appCode = appCode,
    message = message
)

open class UnexpectedErrorHttpException(
    message: String?,
    appCode: Int = 2000
) : ApplicationHttpException(
    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    appCode = appCode,
    message = message
)

open class ForbiddenHttpException(
    message: String?,
    appCode: Int = 2006
) : ApplicationHttpException(
    httpStatus = HttpStatus.FORBIDDEN,
    appCode = appCode,
    message = message
)

open class AccessDeniedHttpException(
    message: String?,
    appCode: Int = 3014
) : ApplicationHttpException(
    httpStatus = HttpStatus.FORBIDDEN,
    appCode = appCode,
    message = message
)

open class WrongCredentialsHttpException(
    message: String?,
    appCode: Int = 3009
) : ApplicationHttpException(
    httpStatus = HttpStatus.UNAUTHORIZED,
    appCode = appCode,
    message = message
)

open class AlreadyInUseHttpException(
    message: String?,
    appCode: Int = 2011
) : ApplicationHttpException(
    httpStatus = HttpStatus.CONFLICT,
    appCode = appCode,
    message = message
)

open class UnprocessableEntityHttpException(
    message: String?,
    appCode: Int = 2003
) : ApplicationHttpException(
    httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
    appCode = appCode,
    message = message
)

open class ExpiredHttpException(
    message: String?,
    appCode: Int = 2012
) : ApplicationHttpException(
    httpStatus = HttpStatus.GONE,
    appCode = appCode,
    message = message
)

open class TimeoutHttpException(
    message: String?,
    appCode: Int = 2004
) : ApplicationHttpException(
    httpStatus = HttpStatus.REQUEST_TIMEOUT,
    appCode = appCode,
    message = message
)

open class NotImplementedHttpException(
    message: String?,
    appCode: Int = 2009
) : ApplicationHttpException(
    httpStatus = HttpStatus.NOT_IMPLEMENTED,
    appCode = appCode,
    message = message
)
