package com.pavelryzh.firewallus.incident.api

import com.pavelryzh.firewallus.incident.service.IncidentService
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.scheduling.annotation.Async
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList

@RestController
@RequestMapping("/api/v1/incidents")
@PreAuthorize("hasRole('ADMIN')")
class IncidentController(private val incidentService: IncidentService) {

    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    @GetMapping("/stream")
    fun streamIncidents(): SseEmitter {
        val emitter = SseEmitter(0L)
        emitters.add(emitter)

        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }

        return emitter
    }

    @Async
    @EventListener
    fun onNewIncident(incidentDto: IncidentResponseDto) {
        val deadEmitters = mutableSetOf<SseEmitter>()

        emitters.forEach { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("new-incident")
                        .data(incidentDto)
                )
            } catch (e: Exception) {
                deadEmitters.add(emitter)
            }
        }

        emitters.removeAll(deadEmitters)
    }

    @GetMapping
    fun getAllIncidents(
        @PageableDefault(size = 20, page = 0) pageable: Pageable
    ): Page<IncidentResponseDto> {

        val incidentsPage = incidentService.getAllIncidents(pageable)
        return incidentsPage.map { it.toDto() }
    }
}