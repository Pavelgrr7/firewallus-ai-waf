package com.pavelryzh.firewallus.settings.service

import com.pavelryzh.firewallus.settings.domain.WafSettings
import com.pavelryzh.firewallus.settings.event.SettingsUpdatedEvent
import com.pavelryzh.firewallus.settings.port.WafSettingsRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WafSettingsService(
    private val settingsRepo: WafSettingsRepository,
    private val eventPublisher: ApplicationEventPublisher

) {

    @Transactional
    fun save(wafSettings: WafSettings): WafSettings {
        wafSettings.id = 1
        val saved = settingsRepo.save(wafSettings)

        eventPublisher.publishEvent(SettingsUpdatedEvent(saved))

        return saved
    }
}