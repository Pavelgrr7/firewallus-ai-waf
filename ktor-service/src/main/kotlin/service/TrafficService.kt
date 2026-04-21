package com.pavelryzh.service

import com.pavelryzh.kafka.KafkaTrafficProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class TrafficService(val kafkaProducer: KafkaTrafficProducer) {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun processTraffic(event: TrafficEventDto) {
        serviceScope.launch {
            try {
                kafkaProducer.send(event)
            } catch (e: Exception) {
                println("Failed to send to Kafka: ${e.message}")
            }
        }
    }

    fun stop() {
        serviceScope.cancel()
    }

}