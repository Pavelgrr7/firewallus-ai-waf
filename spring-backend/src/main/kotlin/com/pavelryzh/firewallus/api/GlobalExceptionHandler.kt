package com.pavelryzh.firewallus.api

import com.pavelryzh.firewallus.rule.domain.RuleNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant
import  org.slf4j.Logger
data class ErrorResponse(
    val error: String,
    val message: String?,
    val timestamp: Instant = Instant.now()
)

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

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
}