package com.pavelryzh.firewallus.api

import com.pavelryzh.firewallus.rule.domain.RuleNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.async.AsyncRequestNotUsableException
import org.springframework.security.access.AccessDeniedException
import java.time.Instant

data class ErrorResponse(
    val error: String,
    val message: String?,
    val timestamp: Instant = Instant.now(),
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
    fun handleRuleNotFound(ex: RuleNotFoundException): ErrorResponse =
        ErrorResponse(
            error = "RULE_NOT_FOUND",
            message = ex.message,
        )

    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleBadCredentialsException(ex: BadCredentialsException): ErrorResponse {
        logger.warn("Bad credentials exception", ex)
        return ErrorResponse(
            error = "BAD_CREDENTIALS",
            message = "Incorrect credentials: check login or password",
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccessDenied(ex: AccessDeniedException): ErrorResponse = ErrorResponse("FORBIDDEN", "Доступ запрещен: недостаточно прав")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ErrorResponse {
        val errorMessage =
            ex.bindingResult.fieldErrors.joinToString("; ") {
                "${it.field}: ${it.defaultMessage}"
            }
        return ErrorResponse("VALIDATION_FAILED", errorMessage)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllExceptions(ex: Exception): ErrorResponse {
        logger.error("Unhandled exception", ex)
        return ErrorResponse(
            error = "INTERNAL_SERVER_ERROR",
            message = "Произошла внутренняя ошибка сервера",
        )
    }
}
