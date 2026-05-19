package com.pavelryzh.firewallus

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class InitSpringFwAiApplication

fun main(args: Array<String>) {
    runApplication<InitSpringFwAiApplication>(*args)
}
