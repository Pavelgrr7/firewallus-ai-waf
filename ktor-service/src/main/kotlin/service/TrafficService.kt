package com.pavelryzh.service

import com.pavelryzh.kafka.KafkaTrafficProducer
import org.apache.kafka.clients.producer.KafkaProducer


class TrafficService(val kafkaProducer: KafkaTrafficProducer) {

    fun processTraffic(event: TrafficEventDto) {
        kafkaProducer.send(event)
    }

}