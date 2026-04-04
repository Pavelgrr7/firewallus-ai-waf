package com.pavelryzh

import io.ktor.server.application.*
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import java.util.Properties

fun Application.configureKafka() {
    val bootstrapServers = environment.config.propertyOrNull("ktor.kafka.bootstrapServers")?.getString()
        ?: "kafka:29092"

    val clientId = environment.config.propertyOrNull("ktor.kafka.producerClientId")?.getString()
        ?: "ktor-gateway-producer"

    val adminProperties = Properties().apply {
        put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(AdminClientConfig.CLIENT_ID_CONFIG, clientId)
        put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000")
    }

    // todo (AdminClient, Consumer, Producer и т.д.)
    try {
        val adminClient = AdminClient.create(adminProperties)
        log.info("Kafka AdminClient successfully created connecting to $bootstrapServers")
        // ... твой код работы с Kafka
    } catch (e: Exception) {
        log.error("Failed to connect to Kafka at $bootstrapServers", e)
    }
}