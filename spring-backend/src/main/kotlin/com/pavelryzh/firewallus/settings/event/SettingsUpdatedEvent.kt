package com.pavelryzh.firewallus.settings.event

import com.pavelryzh.firewallus.settings.domain.WafSettings

data class SettingsUpdatedEvent(
    val rateLimitRequests: Int,
    val rateLimitWindowSec: Int,
    val adaptiveModeEnabled: Boolean
) {
    constructor(entity: WafSettings) : this(
        entity.rateLimitRequests,
        entity.rateLimitWindowSec,
        entity.adaptiveModeEnabled
    )
}