package com.pavelryzh.firewallus.settings.event

import com.pavelryzh.firewallus.settings.domain.WafSettings
import java.util.UUID

data class SettingsUpdatedEvent(
    val rateLimitRequests: Int,
    val rateLimitWindowSec: Int,
    val adaptiveModeEnabled: Boolean,
    val targetUrl: String,
    val adminId: UUID?
) {
    constructor(entity: WafSettings, adminId: UUID?) : this(
        entity.rateLimitRequests,
        entity.rateLimitWindowSec,
        entity.adaptiveModeEnabled,
        entity.targetUrl,
        adminId
    )
}