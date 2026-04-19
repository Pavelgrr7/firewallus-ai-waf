package com.pavelryzh

import com.pavelryzh.plugins.configureKafka
import com.pavelryzh.plugins.configureSerialization
import com.pavelryzh.service.KafkaTrafficProducer
import io.ktor.server.application.*
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureDatabases()
    configureSerialization()
    configureHTTP()
    configureRouting()
    configureKafka()

    val kafkaProducer = KafkaTrafficProducer("kafka:29092")

    // Не забываем закрывать ресурсы при остановке приложения
    environment.monitor.subscribe(ApplicationStopped) {
        kafkaProducer.close()
    }

    routing {
        get("/test-firewall") {
            // Извлекаем метаданные (пока берем то, что видим напрямую)
            val ip = call.request.local.remoteHost
            val method = call.request.httpMethod.value
            val uri = call.request.uri

            // Отправляем тень запроса в Kafka (Fire-and-forget)
            kafkaProducer.sendTrafficLog(ip, method, uri)

            // Сразу отвечаем клиенту, не дожидаясь ответа от Kafka
            call.respondText("Hello, World! Your request is being analyzed out-of-band.")
        }
    }
}
