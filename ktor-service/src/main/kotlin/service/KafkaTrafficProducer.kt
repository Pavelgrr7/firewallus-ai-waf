package com.pavelryzh.service

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.Properties

class KafkaTrafficProducer(bootstrapServers: String) {

    private val producer: KafkaProducer<String, String>

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

    fun sendTrafficLog(ip: String, method: String, uri: String) {
        // todo нормальная сериализация Data Class в JSON
        val jsonPayload = """
            {
                "ip": "$ip",
                "method": "$method",
                "uri": "$uri",
                "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()

        // Все запросы с одного IP попадали - одна партиция Kafka
        val record = ProducerRecord("traffic-logs", ip, jsonPayload)

        // Фоновый поток Kafka I/O.
        producer.send(record) { metadata, exception ->
            if (exception != null) {
                println("[WAF Shadowing] Failed to send log to Kafka: ${exception.message}")
            } else {
                println("[WAF Shadowing] Log sent to partition ${metadata.partition()} at offset ${metadata.offset()}")
            }
        }
    }

    fun close() {
        producer.close()
    }
}