package com.pavelryzh.plugins.di


import io.ktor.client.plugins.HttpRequestRetryConfig
import io.ktor.http.HttpMethod

/**
 * Политика повторных запросов (Retry Policy) для WAF.
 * Защищает от случайного дублирования неидемпотентных операций (POST/PATCH)
 * и фильтрует бессмысленные повторы клиентских ошибок (4xx).
 */

fun HttpRequestRetryConfig.wafRetryPolicy() {
    maxRetries = 3
    exponentialDelay() // Задержки: 1с, 2с, 4с...

    // Список идемпотентных методов
    // PUT и DELETE по HTTP спецификации тоже идемпотентны, но на практике
    // разработчики бэкендов часто реализуют их криво, поэтому оставляем только самые безопасные
    val safeMethods = setOf(HttpMethod.Get, HttpMethod.Head, HttpMethod.Options)

    // Список кодов ответа сервера, при которых ретрай имеет смысл
    val retryableStatusCodes = setOf(
        429, // Too Many Requests
        502, // Bad Gateway
        503, // Service Unavailable
        504  // Gateway Timeout
    )

    retryIf { request, response ->
        val isSafeMethod = request.method in safeMethods
        val isRetryableStatus = response.status.value in retryableStatusCodes

        isSafeMethod && isRetryableStatus
    }

    retryOnExceptionIf { request, _ ->
        // Ошибка 502/504 передаётся клиенту, и пусть он сам решает, кидать ли запрос еще раз
        request.method in safeMethods
    }
}