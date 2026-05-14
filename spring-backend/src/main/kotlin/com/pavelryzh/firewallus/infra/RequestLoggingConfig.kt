package com.pavelryzh.firewallus.infra

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class RequestLoggingConfig {

    @Bean
    fun requestLoggingFilter(): CommonsRequestLoggingFilter {
        val filter = CommonsRequestLoggingFilter()
        filter.setIncludeClientInfo(true)   // Remote address + session id
        filter.setIncludeQueryString(true)  // Query params
        filter.setIncludeHeaders(true)      // All headers (incl. Origin, Authorization)
        filter.setIncludePayload(true)      // Request body
        filter.setMaxPayloadLength(1000)
        filter.setBeforeMessagePrefix(">>> INCOMING REQUEST: ")
        filter.setAfterMessagePrefix("<<< REQUEST DONE: ")
        return filter
    }
}
