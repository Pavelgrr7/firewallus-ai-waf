package com.pavelryzh.firewallus.incident.service

import com.pavelryzh.firewallus.incident.api.IncidentResponseDto
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList

@Service
class IncidentNotificationService {

    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun createEmitter(): SseEmitter {
        val emitter = SseEmitter(0L)
        emitters.add(emitter)

        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }

        return emitter
    }

    @Scheduled(fixedRate = 30000)
    fun sendHeartbeat() {
        val deadEmitters = mutableListOf<SseEmitter>()

        emitters.forEach { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("ping")
                        .data("keep-alive")
                )
            } catch (e: Exception) {
                deadEmitters.add(emitter)
            }
        }

        emitters.removeAll(deadEmitters)
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
}