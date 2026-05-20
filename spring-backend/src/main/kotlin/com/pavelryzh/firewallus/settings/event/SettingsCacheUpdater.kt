package com.pavelryzh.firewallus.settings.event

import com.pavelryzh.firewallus.settings.port.SettingsCache
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class SettingsCacheUpdater(private val cache: SettingsCache) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onSettingsUpdated(event: SettingsUpdatedEvent) {
        cache.saveSettings(event)
    }
}