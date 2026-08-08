package com.pavelryzh.firewallus

import io.gatling.javaapi.core.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*
import kotlin.random.Random

class WafStressSimulation: Simulation() {
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
        .exec(http("Stress Ping").get("/").header("X-Forwarded-For", "#{randomIp}"))

    init {
        println("Starting Load Test on target URL: $targetUrl")
        setUp(
            stressScenario.injectOpen(
                // Плавный разгон от 10 до 1000 запросов в секунду за 3 минуты!
                rampUsersPerSec(10.0).to(1000.0).during(180))
        ).protocols(httpProtocol)
    }
}