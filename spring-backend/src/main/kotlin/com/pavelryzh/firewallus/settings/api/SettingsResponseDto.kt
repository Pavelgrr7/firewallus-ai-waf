package com.pavelryzh.firewallus.settings.api

import com.pavelryzh.firewallus.settings.domain.WafSettings


data class SettingsResponseDto(
    val rateLimitRequests: Int,
    val rateLimitWindowSec: Int,
    val tgBotToken: String?,
    val tgChatId: String?,
    val alertThreshold: Int
)

data class UpdateSettingsDto(
    val rateLimitRequests: Int?,
    val rateLimitWindowSec: Int?,
    val tgBotToken: String? = null,
    val tgChatId: String? = null,
    val alertThreshold: Int? = null,
)

fun WafSettings.toDto(): SettingsResponseDto {
    return SettingsResponseDto(
        rateLimitRequests = rateLimitRequests,
        rateLimitWindowSec = rateLimitWindowSec,
        tgBotToken = this.tgBotToken,
        tgChatId = this.tgChatId,
        alertThreshold = this.alertThreshold,
    )
}