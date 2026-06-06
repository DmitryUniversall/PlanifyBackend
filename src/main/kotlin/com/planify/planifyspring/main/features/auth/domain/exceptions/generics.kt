package com.planify.planifyspring.main.features.auth.domain.exceptions

import com.planify.planifyspring.core.exceptions.TooManyRequestsAppError
import com.planify.planifyspring.main.exceptions.generics.BadRequestHttpException
import com.planify.planifyspring.main.exceptions.generics.InternalServerErrorHttpException
import com.planify.planifyspring.main.exceptions.generics.TooManyRequestsHttpException
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

class AuthorizationNotSpecifiedHttpException(
    message: String?
) : UnauthorizedHttpException(
    appCode = 3001,
    message = message
)

class AuthorizationTypeUnknownHttpException(
    message: String?
) : UnauthorizedHttpException(
    appCode = 3003,
    message = message
)


class AuthorizationTokenNotSpecifiedHttpException(
    message: String?
) : UnauthorizedHttpException(
    appCode = 3004,
    message = message
)

class AuthorizationFailedHttpException(
    message: String?
) : UnauthorizedHttpException(
    appCode = 3013,
    message = message
)

class UnknownUserHttpException(
    message: String?
) : UnauthorizedHttpException(
    appCode = 3008,
    message = message
)

class InactiveSessionHttpException(
    message: String?
) : UnauthorizedHttpException(
    appCode = 3011,
    message = message
)

class InvalidRegisterConfirmationCodeHttpException(
    message: String? = "Confirmation code invalid"
) : UnauthorizedHttpException(
    appCode = 3015,  // TODO: Change status codes to 6XXX
    message = message
)

class ExpiredRegisterConfirmationCodeHttpException(
    message: String = "Confirmation code expired"
) : BadRequestHttpException(
    appCode = 3016,
    message = message
)

class RecoverPasswordChallengeFailedHttpException(
    message: String = "Recover password challenge failed"
) : BadRequestHttpException(
    appCode = 3017,
    message = message
)

class RecoverPasswordChallengeAlreadyPassedHttpException(
    message: String = "Recover password challenge already passed"
) : BadRequestHttpException(
    appCode = 3018,
    message = message
)

class RecoverPasswordChallengeAttemptFailedHttpException(
    message: String = "Recover password challenge attempt failed"
) : BadRequestHttpException(
    appCode = 3019,
    message = message
)

class RecoverPasswordChallengeNotPassedHttpException(
    message: String = "Recover password challenge not passed"
) : BadRequestHttpException(
    appCode = 3020,
    message = message
)

class BadRecoverPasswordChallengeStateHttpException(
    message: String = "Bad password challenge state"
) : InternalServerErrorHttpException(
    appCode = 3021,
    message = message
)

class PasswordRecoveryRateLimitHttpException(
    message: String = "Password recovery requested too soon, please try again later"
) : TooManyRequestsHttpException(
    appCode = 3022,
    message = message
)

class PasswordRecoveryInProcessHttpException(
    message: String = "Password recovery was already requested, please try again later"
) : BadRequestHttpException(
    appCode = 3023,
    message = message
)
