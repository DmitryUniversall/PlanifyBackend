package com.planify.planifyspring.main.features.favorites.domain.exceptions

import com.planify.planifyspring.main.exceptions.generics.BadRequestHttpException

class UserAlreadyInFavoritesHttpException(
    message: String?
) : BadRequestHttpException(
    appCode = 4010,
    message = message
)
