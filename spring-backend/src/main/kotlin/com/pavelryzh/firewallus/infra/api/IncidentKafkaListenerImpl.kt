package com.pavelryzh.firewallus.infra.api

import com.pavelryzh.firewallus.incident.api.IncidentEventDto
import com.pavelryzh.firewallus.incident.port.IncidentListener
import com.pavelryzh.firewallus.incident.service.IncidentService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory

@Component
class IncidentKafkaListenerImpl(
    private val incidentService: IncidentService,
    private val objectMapper: ObjectMapper
): IncidentListener {
    
    private val logger = LoggerFactory.getLogger(IncidentKafkaListenerImpl::class.java)

    @KafkaListener(topics = ["incidents"], groupId = "spring-manager-group")
    fun consumeIncident(message: String) {
        try {
            val eventDto = objectMapper.readValue(message, IncidentEventDto::class.java)
            logger.debug("Прочтено сообщение из incidents, $message")
            incidentService.registerIncident(eventDto)
        } catch(e: Exception) {
            logger.error("КРИТИЧЕСКАЯ ОШИБКА ПАРСИНГА ИЗ KAFKA")
            logger.error("Сырое сообщение: $message")
            logger.warn("Ошибка: ${e.message}")
        }

    }
}