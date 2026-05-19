package com.pavelryzh.kafka

import com.pavelryzh.plugins.logger
import com.pavelryzh.service.dto.KafkaEvent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

@OptIn(ExperimentalSerializationApi::class)
class KafkaTrafficProducer(
    bootstrapServers: String,
): AutoCloseable {

    @PublishedApi
    internal val producer: KafkaProducer<String, String>

    @PublishedApi
    internal val logger = logger()

    @PublishedApi
    internal val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.SnakeCase

    }

    init {
        val props = Properties().apply {
            put("bootstrap.servers", bootstrapServers)
            put("client.id", "ktor-gateway-producer")
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)

            // Out-of-Band
            put("acks", "0")
            put("linger.ms", "5") // Батчи по 5 мс
        }
        producer = KafkaProducer(props)
    }

    inline fun <reified T : KafkaEvent> send(topic: String, message: T) {
        val jsonPayload = runCatching {
            json.encodeToString(message)
        }.getOrElse {
            logger.error("Failed to serialize Kafka event: ${it.message}")
            return
        }

        val record = ProducerRecord<String, String>(topic, jsonPayload)

        producer.send(record) { metadata, exception ->
            if (exception != null) {
                logger.error("Error sending to Kafka topic '$topic': ${exception.message}")
            } else {
                logger.debug("Log sent to partition ${metadata.partition()} at offset ${metadata.offset()}")
            }
        }

    }

    override fun close() {
        producer.close()
    }
}