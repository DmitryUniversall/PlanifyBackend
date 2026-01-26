package com.planify.planifyspring.main.exceptions.handlers

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.exceptions.ApplicationHttpException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

class GlobalExceptionHandler {
    companion object {
        fun buildErrorResponse(
            error: Exception,
            status: HttpStatus,
            appCode: Int,
            message: String? = null,
        ): ResponseEntity<ApplicationResponse<Nothing>> {
            return ResponseEntity(
                ApplicationResponse(
                    ok = false,
                    appCode = appCode,
                    message = "[${status.value()}] ${status.reasonPhrase}: ${message ?: error.message}",
                    data = null
                ),
                status
            )
        }
    }

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception, request: WebRequest): ResponseEntity<ApplicationResponse<Nothing>> {
        logger.error("Unexpected error occurred", e)

        return buildErrorResponse(
            error = e,
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            appCode = 2000,
            message = "Unexpected error occurred"
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        e: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        val cause = e.cause
        return if (cause is InvalidFormatException) {
            buildErrorResponse(
                error = cause,
                status = HttpStatus.BAD_REQUEST,
                appCode = 2005,
                message = "Bad request payload format: failed to parse"
            )
        } else {
            buildErrorResponse(
                error = e,
                status = HttpStatus.BAD_REQUEST,
                appCode = 2005,
                message = "Bad request payload"
            )
        }
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(
        e: HttpRequestMethodNotSupportedException,
        request: WebRequest
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        return buildErrorResponse(
            error = e,
            status = HttpStatus.METHOD_NOT_ALLOWED,
            appCode = 2008  // TODO: Create method not allowed appcode
        )
    }

    @ExceptionHandler(ApplicationHttpException::class)
    fun handleApplicationHttpException(
        e: ApplicationHttpException,
        request: WebRequest
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        return buildErrorResponse(
            error = e,
            status = e.httpStatus,
            appCode = e.appCode,
            message = e.message
        )
    }
}
