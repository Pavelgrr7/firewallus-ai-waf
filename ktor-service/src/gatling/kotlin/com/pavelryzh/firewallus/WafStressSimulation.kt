package com.pavelryzh.firewallus

import io.gatling.javaapi.core.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*
import kotlin.random.Random

class WafStressSimulation: Simulation() {
    // ./gradlew gatlingRun --simulation com.pavelryzh.firewallus.WafStressSimulation -DwafTargetUrl=http://127.0.0.1:3000 | tee gatling-summary.txt
    private val targetUrl = System.getenv("WAF_TARGET_URL")
        ?: System.getProperty("wafTargetUrl")
        ?: "http://localhost:80"


    private val httpProtocol = http
        .baseUrl(targetUrl)
        .acceptHeader("application/json")

    val randomIpFeeder = generateSequence {
        mapOf("randomIp" to "${Random.nextInt(1, 255)}.${Random.nextInt(0, 255)}.${Random.nextInt(0, 255)}.${Random.nextInt(1, 255)}")
    }.iterator()

    private val stressScenario = scenario("Stress Test")
        .feed(randomIpFeeder)
        .repeat(200).on(
            exec(http("Stress Ping").get("/").header("X-Forwarded-For", "#{randomIp}"))
        )

    init {
        setUp(
            // 450 пользователей
            // Сгенерируют 90 000 запросов
            stressScenario.injectClosed(constantConcurrentUsers(450).during(180))
        ).protocols(httpProtocol)
    }
}