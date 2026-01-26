package com.planify.planifyspring.main.common.utils

import com.planify.planifyspring.main.common.entities.ApplicationResponse

fun <T> T.asSuccessResponse(
    message: String = "Success",
    statusCode: Int = 1000
): ApplicationResponse<T> = ApplicationResponse(
    ok = true,
    appCode = statusCode,
    message = message,
    data = this
)

fun asErrorResponse(
    message: String,
    statusCode: Int
): ApplicationResponse<Nothing> = ApplicationResponse(
    ok = false,
    appCode = statusCode,
    message = message,
    data = null
)
