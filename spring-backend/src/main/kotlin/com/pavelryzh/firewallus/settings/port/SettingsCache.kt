package com.pavelryzh.firewallus.settings.port

import com.pavelryzh.firewallus.settings.event.SettingsUpdatedEvent

interface SettingsCache {
    fun saveSettings(event: SettingsUpdatedEvent)
}