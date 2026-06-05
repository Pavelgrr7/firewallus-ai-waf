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
    @Value($$"${waf.default.rate-limit.window:60}") private val defaultWindow: Int,
    @Value($$"${waf.seed.default.target-url:http://target-backend:8080}") private val defaultTargetUrl: String
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(WafSettingsSeeder::class.java)

    @Transactional
    override fun run(args: ApplicationArguments) {
        val settings = if (settingsRepository.count() == 0L) {
            logger.info("Инициализация глобальных настроек WAF...")

            val newSettings = WafSettings(
                rateLimitRequests = defaultRequests,
                rateLimitWindowSec = defaultWindow,
                adaptiveModeEnabled = false,
                targetUrl = defaultTargetUrl
            )
            newSettings.id = 1

            settingsRepository.save(newSettings)
            logger.info("Настройки WAF установлены по умолчанию (Limit: $defaultRequests / ${defaultWindow}s).")
            newSettings
        } else {
            val existingSettings = settingsRepository.findById(1).get()
            var modified = false
            if (existingSettings.targetUrl != defaultTargetUrl) {
                existingSettings.targetUrl = defaultTargetUrl
                modified = true
                logger.info("Обновлен targetUrl в БД на $defaultTargetUrl")
            }
            if (existingSettings.rateLimitRequests != defaultRequests) {
                existingSettings.rateLimitRequests = defaultRequests
                modified = true
                logger.info("Обновлен лимит запросов в БД на $defaultRequests")
            }
            if (existingSettings.rateLimitWindowSec != defaultWindow) {
                existingSettings.rateLimitWindowSec = defaultWindow
                modified = true
                logger.info("Обновлено окно лимита в БД на $defaultWindow")
            }
            if (modified) {
                settingsRepository.save(existingSettings)
            }
            existingSettings
        }

        eventPublisher.publishEvent(SettingsUpdatedEvent(settings, null))
        logger.info("Глобальные настройки WAF синхронизированы с Redis (Target URL: ${settings.targetUrl}).")
    }
}