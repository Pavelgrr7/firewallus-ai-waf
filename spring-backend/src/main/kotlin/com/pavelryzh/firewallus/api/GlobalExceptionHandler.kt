package com.pavelryzh.firewallus.api

import com.pavelryzh.firewallus.rule.domain.RuleNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant
import  org.slf4j.Logger
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.async.AsyncRequestNotUsableException

data class ErrorResponse(
    val error: String,
    val message: String?,
    val timestamp: Instant = Instant.now()
)

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AsyncRequestNotUsableException::class)
    fun handleAsyncRequestNotUsableException(ex: AsyncRequestNotUsableException) {
        // Это штатное поведение: пользователь просто ушел с дашборда
    }

    @ExceptionHandler(RuleNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleRuleNotFound(ex: RuleNotFoundException): ErrorResponse {
        return ErrorResponse(
            error = "RULE_NOT_FOUND",
            message = ex.message
        )
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllExceptions(ex: Exception): ErrorResponse {
        logger.error("Unhandled exception", ex)
        return ErrorResponse(
            error = "INTERNAL_SERVER_ERROR",
            message = "Произошла внутренняя ошибка сервера"
        )
    }

    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleBadCredentialsException(ex: BadCredentialsException): ErrorResponse {
        logger.error("Bad credentials exception", ex)
        return ErrorResponse(
            error = "Bad credentials exception",
            message = ex.message
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccessDenied(ex: AccessDeniedException): ErrorResponse {
        return ErrorResponse("FORBIDDEN", "Доступ запрещен: недостаточно прав")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ErrorResponse {
        val errorMessage = ex.bindingResult.fieldErrors.joinToString("; ") {
            "${it.field}: ${it.defaultMessage}"
        }
        return ErrorResponse("VALIDATION_FAILED", errorMessage)
    }


}