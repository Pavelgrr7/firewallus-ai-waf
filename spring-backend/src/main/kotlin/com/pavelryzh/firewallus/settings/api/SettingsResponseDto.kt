package com.pavelryzh.firewallus.settings.api

import com.pavelryzh.firewallus.settings.domain.WafSettings


data class SettingsResponseDto(
    val rateLimitRequests: Int,
    val rateLimitWindowSec: Int
)

data class UpdateSettingsDto(
    val rateLimitRequests: Int?,
    val rateLimitWindowSec: Int?
)

fun WafSettings.toDto(): SettingsResponseDto {
    return SettingsResponseDto(
        rateLimitRequests = rateLimitRequests,
        rateLimitWindowSec = rateLimitWindowSec
    )
}