package com.pavelryzh

import io.github.flaxoos.ktor.server.plugins.kafka.*
import io.ktor.client.*
import io.ktor.server.application.*

fun Application.configureDatabases() {
    install(Kafka) {
        schemaRegistryUrl = "my.schemaRegistryUrl"
        val myTopic = TopicName.named("my-topic")
        topic(myTopic) {
            partitions = 1
            replicas = 1
            configs {
                messageTimestampType = MessageTimestampType.CreateTime
            }
        }
        common {
            bootstrapServers = listOf("my-kafka")
            retries = 1
            clientId = "my-client-id"
        }
        admin { }
        producer {
            clientId = "my-client-id"
        }
        consumer {
            groupId = "my-group-id"
            clientId = "my-client-id-override"
        }
        consumerConfig {
            consumerRecordHandler(myTopic) { record ->
                // Do something with record
            }
        }
        registerSchemas {
            using {
                HttpClient()
            }
            // MyRecord::class at myTopic // <-- Will register schema upon startup
        }
    }
}
