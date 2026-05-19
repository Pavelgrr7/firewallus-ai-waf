package com.pavelryzh.firewallus.infra.api

import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class RequestLoggingConfig {

    @Bean
    fun requestLoggingFilter(): CommonsRequestLoggingFilter {
        // Sensitive endpoints (auth) are excluded from header/payload logging
        // to prevent credentials and JWT tokens from appearing in logs.
        val filter = object : CommonsRequestLoggingFilter() {
            private val sensitiveUris = listOf("/api/v1/auth/")

            override fun shouldLog(request: HttpServletRequest): Boolean =
                logger.isDebugEnabled

            override fun createMessage(request: HttpServletRequest, prefix: String, suffix: String): String {
                val isSensitive = sensitiveUris.any { request.requestURI.startsWith(it) }
                setIncludeHeaders(!isSensitive)   // skip headers (incl. Authorization) on auth endpoints
                setIncludePayload(!isSensitive)   // skip body (credentials) on auth endpoints
                return super.createMessage(request, prefix, suffix)
            }
        }

        filter.setIncludeClientInfo(true)
        filter.setIncludeQueryString(true)
        filter.setIncludeHeaders(true)
        filter.setIncludePayload(true)
        filter.setMaxPayloadLength(500)
        filter.setBeforeMessagePrefix(">>> INCOMING REQUEST: ")
        filter.setAfterMessagePrefix("<<< REQUEST DONE: ")
        return filter
    }
}
