package com.pavelryzh.firewallus.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper

@Configuration
class JacksonConfig {

    @Bean
    fun objectMapper(): ObjectMapper {

        val mapper = jacksonObjectMapper()

        // mapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        // mapper.findAndRegisterModules() // для поддержки Java Time (Instant, LocalDate)

        return mapper
    }
}