package com.pavelryzh.firewallus.incident.api

import com.pavelryzh.firewallus.incident.service.IncidentNotificationService
import com.pavelryzh.firewallus.incident.service.IncidentService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


@RestController
@RequestMapping("/api/v1/incidents")
@PreAuthorize("hasRole('ADMIN')")
class IncidentController(private val incidentService: IncidentService,
                         private val notificationService: IncidentNotificationService
) {

    @GetMapping("/stream")
    fun streamIncidents(): SseEmitter {
        return notificationService.createEmitter()
    }

    @GetMapping
    fun getAllIncidents(
        @PageableDefault(size = 20, page = 0, sort = ["timestamp"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<IncidentResponseDto> {

        val incidentsPage = incidentService.getAllIncidents(pageable)
        return incidentsPage.map { it.toDto() }
    }
}