package com.pavelryzh.firewallus.incident.service

import com.pavelryzh.firewallus.incident.api.IncidentEventDto
import com.pavelryzh.firewallus.incident.api.IncidentStatsDto
import com.pavelryzh.firewallus.incident.api.NameValueDto
import com.pavelryzh.firewallus.incident.api.toDto
import com.pavelryzh.firewallus.incident.api.toEntity
import com.pavelryzh.firewallus.incident.domain.Incident
import com.pavelryzh.firewallus.incident.port.IncidentRepository
import com.pavelryzh.firewallus.rule.domain.IpAddress
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
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

    @Transactional(readOnly = true)
    fun getStats(): IncidentStatsDto {
        val summary = incidentRepo.getIncidentStats()

        val attackDist = incidentRepo.getAttackDistribution().map { row ->
            NameValueDto(row[0].toString(), (row[1] as Number).toLong())
        }

        val actionMetricsList = incidentRepo.getActionMetrics().map { row ->
            NameValueDto(row[0].toString(), (row[1] as Number).toLong())
        }

        val topIps = incidentRepo.getTopBlockedIps(PageRequest.of(0, 5)).map { row ->
            val ipStr = when (val ipObj = row[0]) {
                is IpAddress -> ipObj.value
                else -> ipObj.toString()
            }
            NameValueDto(ipStr, (row[1] as Number).toLong())
        }

        return IncidentStatsDto(
            total = summary.total,
            mlBlocked = summary.mlBlocked,
            staticBlocked = summary.staticBlocked,
            allowed = summary.allowed,
            attackDistribution = attackDist,
            topBlockedIps = topIps,
            actionMetrics = actionMetricsList
        )
    }
}