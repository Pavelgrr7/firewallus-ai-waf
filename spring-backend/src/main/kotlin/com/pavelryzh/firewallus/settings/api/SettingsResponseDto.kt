package com.pavelryzh.firewallus.settings.api

import com.pavelryzh.firewallus.settings.domain.WafSettings
import jakarta.validation.constraints.Pattern


data class SettingsResponseDto(
    val rateLimitRequests: Int,
    val rateLimitWindowSec: Int,
    val tgBotToken: String?,
    val tgChatId: String?,
    val alertThreshold: Int,
    val targetUrl: String,
)

data class UpdateSettingsDto(
    val rateLimitRequests: Int?,
    val rateLimitWindowSec: Int?,
    val tgBotToken: String? = null,
    val tgChatId: String? = null,
    val alertThreshold: Int? = null,
    @field:Pattern(
        regexp = "^https?://.*",
        message = "URL бэкенда должен начинаться с http:// или https://"
    )
    val targetUrl: String? = null
)

fun WafSettings.toDto(): SettingsResponseDto {
    return SettingsResponseDto(
        rateLimitRequests = rateLimitRequests,
        rateLimitWindowSec = rateLimitWindowSec,
        tgBotToken = this.tgBotToken,
        tgChatId = this.tgChatId,
        alertThreshold = this.alertThreshold,
        targetUrl = this.targetUrl
    )
}