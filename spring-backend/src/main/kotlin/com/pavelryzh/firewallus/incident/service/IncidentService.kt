package com.pavelryzh.firewallus.incident.service

import com.pavelryzh.firewallus.incident.api.IncidentEventDto
import com.pavelryzh.firewallus.incident.api.toDto
import com.pavelryzh.firewallus.incident.api.toEntity
import com.pavelryzh.firewallus.incident.domain.Incident
import com.pavelryzh.firewallus.incident.port.IncidentRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
class IncidentService(
    private val incidentRepo: IncidentRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun registerIncident(dto: IncidentEventDto): Incident {
        val savedIncident = incidentRepo.save(dto.toEntity())

        eventPublisher.publishEvent(savedIncident.toDto())

        return savedIncident
    }

    @Transactional(readOnly = true)
    fun getAllIncidents(pageable: Pageable): Page<Incident> {
        return incidentRepo.findAll(pageable)
    }


}