package com.pavelryzh.kafka

import com.pavelryzh.plugins.logger
import com.pavelryzh.service.TrafficEventDto
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.Properties

class KafkaTrafficProducer(bootstrapServers: String): AutoCloseable {

    private val producer: KafkaProducer<String, String>
    private val logger = logger()

    init {
        val props = Properties().apply {
            put("bootstrap.servers", bootstrapServers)
            put("client.id", "ktor-gateway-producer")
            put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

            // Out-of-Band
            put("acks", "0")
            put("linger.ms", "5") // Батчи по 5 мс
        }
        producer = KafkaProducer(props)
    }

    fun send(event: TrafficEventDto) {
        val jsonPayload = Json.encodeToString(event)
        // На данный момент нет логики отправки тела запроса (либо части тела запроса при привышении лимита)
        // Сейчас цель - показать рабочий роут и корректную отправку данных в кафку.
        // Позже отдельным PR будет реализована вся логика сбора и отправки информации о запросах

        // Все запросы с одного IP попадали - одна партиция Kafka
        val record = ProducerRecord("traffic-logs", event.ip, jsonPayload)
        // Фоновый поток Kafka I/O
        producer.send(record) { metadata, exception ->
            if (exception != null) {
                logger.info("[WAF Shadowing] Failed to send log to Kafka: ${exception.message}")
            } else {
                logger.info("[WAF Shadowing] Log sent to partition ${metadata.partition()} at offset ${metadata.offset()}")
            }
        }
    }

    override fun close() {
        producer.close()
    }
}