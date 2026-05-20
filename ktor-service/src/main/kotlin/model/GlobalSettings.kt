package com.pavelryzh.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GlobalSettings(
    @SerialName("rate_limit_requests") val limit: Int = 2000,
    @SerialName("rate_limit_window_sec") val window: Int = 60,
    @SerialName("adaptive_mode_enabled") val adaptiveMode: Boolean = false
)