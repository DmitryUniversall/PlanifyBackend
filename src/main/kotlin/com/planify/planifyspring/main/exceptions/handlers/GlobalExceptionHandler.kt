package com.planify.planifyspring.main.exceptions.handlers

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import com.planify.planifyspring.core.exceptions.AccessDeniedAppError
import com.planify.planifyspring.core.exceptions.AlreadyExistsAppError
import com.planify.planifyspring.core.exceptions.AlreadyInUseAppError
import com.planify.planifyspring.core.exceptions.ApplicationException
import com.planify.planifyspring.core.exceptions.ExpiredAppError
import com.planify.planifyspring.core.exceptions.InvalidArgumentAppError
import com.planify.planifyspring.core.exceptions.NotFoundAppError
import com.planify.planifyspring.core.exceptions.TooManyRequestsAppError
import com.planify.planifyspring.core.exceptions.UnprocessableEntityAppError
import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.exceptions.ApplicationHttpException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException


@Suppress("unused")
@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        fun buildErrorResponse(error: Exception, status: HttpStatus, appCode: Int, message: String? = null): ResponseEntity<ApplicationResponse<Nothing>> {
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

        return buildErrorResponse(error = e, status = HttpStatus.INTERNAL_SERVER_ERROR, appCode = 2000, message = "Unexpected error occurred")
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        e: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        val cause = e.cause

        return when (cause) {
            is InvalidFormatException -> {
                buildErrorResponse(error = cause, status = HttpStatus.BAD_REQUEST, appCode = 2003, message = "Bad request payload format: failed to parse")
            }

            is MismatchedInputException, is ValueInstantiationException -> {
                val fields = cause.path.mapNotNull { it.fieldName }.joinToString(", ")
                buildErrorResponse(error = cause, status = HttpStatus.BAD_REQUEST, appCode = 2003, message = "Bad request payload: invalid fields: $fields")
            }

            else -> {
                buildErrorResponse(error = e, status = HttpStatus.BAD_REQUEST, appCode = 2003, message = "Bad request payload")
            }
        }
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException, request: WebRequest): ResponseEntity<ApplicationResponse<Nothing>> {
        return buildErrorResponse(error = e, status = HttpStatus.METHOD_NOT_ALLOWED, appCode = 2010)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(e: NoHandlerFoundException, request: WebRequest): ResponseEntity<ApplicationResponse<Nothing>> {
        return buildErrorResponse(error = e, status = HttpStatus.NOT_FOUND, appCode = 2008, message = "Route does not exists")
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException, request: WebRequest): ResponseEntity<ApplicationResponse<Nothing>> {
        return buildErrorResponse(error = e, status = HttpStatus.BAD_REQUEST, appCode = 2005, message = "Required request parameter '${e.parameterName}' is not present")
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ApplicationResponse<Nothing>> {
        return buildErrorResponse(error = e, status = HttpStatus.BAD_REQUEST, appCode = 2003, message = "Bad url or parameter argument format")
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDeniedException(e: AuthorizationDeniedException): ResponseEntity<ApplicationResponse<Nothing>> {
        return buildErrorResponse(error = e, status = HttpStatus.FORBIDDEN, appCode = 3014, message = "Access denied")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ApplicationResponse<Nothing>> {
        return buildErrorResponse(error = e, status = HttpStatus.BAD_REQUEST, appCode = 2003, message = e.bindingResult.fieldErrors.joinToString("; ") { it.defaultMessage ?: "Invalid value" })
    }

    @ExceptionHandler(NotFoundAppError::class)
    fun handleNotFoundAppError(e: NotFoundAppError): ResponseEntity<ApplicationResponse<Nothing>> {
        logger.debug("NotFoundAppError: {}", e.message)
        return buildErrorResponse(error = e, status = HttpStatus.NOT_FOUND, appCode = 2002, message = e.message)
    }

    @ExceptionHandler(AlreadyExistsAppError::class)
    fun handleAlreadyExistsAppError(e: AlreadyExistsAppError): ResponseEntity<ApplicationResponse<Nothing>> {
        logger.debug("AlreadyExistsAppError: {}", e.message)
        return buildErrorResponse(error = e, status = HttpStatus.CONFLICT, appCode = 2001, message = e.message)
    }

    @ExceptionHandler(AlreadyInUseAppError::class)
    fun handleAlreadyInUseAppError(e: AlreadyInUseAppError): ResponseEntity<ApplicationResponse<Nothing>> {
        logger.debug("AlreadyInUseAppError: {}", e.message)
        return buildErrorResponse(error = e, status = HttpStatus.CONFLICT, appCode = 2011, message = e.message)
    }

    @ExceptionHandler(InvalidArgumentAppError::class)
    fun handleInvalidArgumentAppError(e: InvalidArgumentAppError): ResponseEntity<ApplicationResponse<Nothing>> {
        logger.debug("InvalidArgumentAppError: {}", e.message)
        return buildErrorResponse(error = e, status = HttpStatus.BAD_REQUEST, appCode = 2005, message = e.message)
    }

    @ExceptionHandler(AccessDeniedAppError::class)
    fun handleAccessDeniedAppError(e: AccessDeniedAppError): ResponseEntity<ApplicationResponse<Nothing>> {
        logger.debug("AccessDeniedAppError: {}", e.message)
        return buildErrorResponse(error = e, status = HttpStatus.FORBIDDEN, appCode = 3014, message = e.message)
    }

    @ExceptionHandler(ExpiredAppError::class)
    fun handleExpiredAppError(e: ExpiredAppError): ResponseEntity<ApplicationResponse<Nothing>> {
        logger.debug("ExpiredAppError: {}", e.message)
        return buildErrorResponse(error = e, status = HttpStatus.GONE, appCode = 2012, message = e.message)
    }

    @ExceptionHandler(TooManyRequestsAppError::class)
    fun handleExpiredAppError(e: TooManyRequestsAppError): ResponseEntity<ApplicationResponse<Nothing>> {
        logger.debug("TooManyRequestsAppError: {}", e.message)
        return buildErrorResponse(error = e, status = HttpStatus.TOO_MANY_REQUESTS, appCode = 2013, message = e.message)
    }

    @ExceptionHandler(UnprocessableEntityAppError::class)
    fun handleUnprocessableEntityAppError(e: UnprocessableEntityAppError): ResponseEntity<ApplicationResponse<Nothing>> {
        logger.debug("UnprocessableEntityAppError: {}", e.message)
        return buildErrorResponse(error = e, status = HttpStatus.UNPROCESSABLE_ENTITY, appCode = 2003, message = e.message)
    }

    @ExceptionHandler(ApplicationException::class)
    fun handleApplicationException(e: ApplicationException, request: WebRequest): ResponseEntity<ApplicationResponse<Nothing>> {
        logger.warn("Unmapped application error occurred", e)
        return buildErrorResponse(error = e, status = HttpStatus.INTERNAL_SERVER_ERROR, appCode = 2000, message = "Internal server error")
    }

    @ExceptionHandler(ApplicationHttpException::class)
    fun handleApplicationHttpException(e: ApplicationHttpException, request: WebRequest): ResponseEntity<ApplicationResponse<Nothing>> {
        return buildErrorResponse(error = e, status = e.httpStatus, appCode = e.appCode, message = e.message)
    }
}
