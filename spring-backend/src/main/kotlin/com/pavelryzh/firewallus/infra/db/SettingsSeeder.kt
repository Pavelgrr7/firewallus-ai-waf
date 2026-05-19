package com.pavelryzh.firewallus.infra.db


import com.pavelryzh.firewallus.settings.domain.WafSettings
import com.pavelryzh.firewallus.settings.event.SettingsUpdatedEvent
import com.pavelryzh.firewallus.settings.port.WafSettingsRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class WafSettingsSeeder(
    private val settingsRepository: WafSettingsRepository,
    private val eventPublisher: ApplicationEventPublisher,
    @Value($$"${waf.default.rate-limit.requests:2000}") private val defaultRequests: Int,
    @Value($$"${waf.default.rate-limit.window:60}") private val defaultWindow: Int
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(WafSettingsSeeder::class.java)

    @Transactional
    override fun run(args: ApplicationArguments) {
        if (settingsRepository.count() == 0L) {
            logger.info("Инициализация глобальных настроек WAF...")

            val settings = WafSettings(
                rateLimitRequests = defaultRequests,
                rateLimitWindowSec = defaultWindow,
                adaptiveModeEnabled = false
            )
            settings.id = 1

            settingsRepository.save(settings)

            eventPublisher.publishEvent(SettingsUpdatedEvent(settings))

            logger.info("Настройки WAF установлены по умолчанию (Limit: $defaultRequests / ${defaultWindow}s).")
        }
    }
}