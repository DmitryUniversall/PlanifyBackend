package com.planify.planifyspring.main.features.auth.domain.exceptions

import com.planify.planifyspring.main.exceptions.generics.UnauthorizedHttpException

class TokenInvalidHttpException(
    message: String?
) : UnauthorizedHttpException(
    appCode = 3006,
    message = message
)

class SuspiciousActivityDetectedHttpException(
    message: String?
) : UnauthorizedHttpException(
    appCode = 3012,
    message = message
)

class TokenExpiredHttpException(
    message: String?
) : UnauthorizedHttpException(
    appCode = 3005,
    message = message
)

class InvalidSessionHttpException(
    message: String?
) : UnauthorizedHttpException(
    appCode = 3010,
    message = message
)
