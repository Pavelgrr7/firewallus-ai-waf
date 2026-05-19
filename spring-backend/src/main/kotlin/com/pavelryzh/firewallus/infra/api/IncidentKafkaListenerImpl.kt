package com.pavelryzh.firewallus.infra.api

import com.pavelryzh.firewallus.incident.api.IncidentEventDto
import com.pavelryzh.firewallus.incident.port.IncidentListener
import com.pavelryzh.firewallus.incident.service.IncidentService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class IncidentKafkaListenerImpl(
    private val incidentService: IncidentService,
    private val objectMapper: ObjectMapper
): IncidentListener {

    @KafkaListener(topics = ["incidents"], groupId = "spring-manager-group")
    fun consumeIncident(message: String) {
        val eventDto = objectMapper.readValue(message, IncidentEventDto::class.java)
        incidentService.registerIncident(eventDto)

    }
}