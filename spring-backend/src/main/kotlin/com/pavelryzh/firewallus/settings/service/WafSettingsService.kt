package com.pavelryzh.firewallus.settings.service

import com.pavelryzh.firewallus.settings.domain.WafSettings
import com.pavelryzh.firewallus.settings.port.WafSettingsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WafSettingsService(private val settingsRepo: WafSettingsRepository) {

    @Transactional
    fun save(wafSettings: WafSettings): WafSettings {
        return settingsRepo.save(wafSettings)
    }
}