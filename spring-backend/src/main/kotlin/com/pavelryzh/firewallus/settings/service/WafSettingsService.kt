package com.pavelryzh.firewallus.settings.service

import com.pavelryzh.firewallus.exception.ResourceNotFoundException
import com.pavelryzh.firewallus.infra.security.CurrentAdminProvider
import com.pavelryzh.firewallus.settings.api.UpdateSettingsDto
import com.pavelryzh.firewallus.settings.domain.WafSettings
import com.pavelryzh.firewallus.settings.event.SettingsUpdatedEvent
import com.pavelryzh.firewallus.settings.port.WafSettingsRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WafSettingsService(
    private val settingsRepo: WafSettingsRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val currentAdminProvider: CurrentAdminProvider,

    ) {
    companion object {
        private const val SETTINGS_ID = 1
    }

    @Transactional
    fun save(wafSettings: WafSettings): WafSettings {
        wafSettings.id = SETTINGS_ID
        val saved = settingsRepo.save(wafSettings)

        val adminId = currentAdminProvider.getCurrentAdminId()

        eventPublisher.publishEvent(SettingsUpdatedEvent(saved, adminId))

        return saved
    }

    @Transactional(readOnly = true)
    fun getSettings(): WafSettings {
        return settingsRepo.findById(SETTINGS_ID).orElseThrow { ResourceNotFoundException("Global WAF settings not found") }
    }

    @Transactional
    fun updateSettings(updateDto: UpdateSettingsDto): WafSettings {
        val settings = settingsRepo.findById(SETTINGS_ID).orElseThrow { ResourceNotFoundException("Global WAF settings not found") }

        updateDto.rateLimitRequests?.let { settings.rateLimitRequests = it }
        updateDto.rateLimitWindowSec?.let { settings.rateLimitWindowSec = it }

        updateDto.tgBotToken?.let { settings.tgBotToken = it }
        updateDto.tgChatId?.let { settings.tgChatId = it }
        updateDto.alertThreshold?.let { settings.alertThreshold = it }
        updateDto.targetUrl?.let { settings.targetUrl = it }

        val adminId = currentAdminProvider.getCurrentAdminId()

        eventPublisher.publishEvent(SettingsUpdatedEvent(settings, adminId))

        return settings
    }
}