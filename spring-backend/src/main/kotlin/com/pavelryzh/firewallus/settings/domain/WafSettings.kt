package com.pavelryzh.firewallus.settings.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "waf_settings")
class WafSettings(
    @Column(name = "rate_limit_requests")
    var rateLimitRequests: Int,

    @Column(name = "rate_limit_window_sec")
    var rateLimitWindowSec: Int,

    @Column(name = "adaptive_mode_enabled")
    var adaptiveModeEnabled: Boolean,

    @Column(name = "tg_bot_token")
    var tgBotToken: String? = null,

    @Column(name = "tg_chat_id")
    var tgChatId: String? = null,

    @Column(name = "alert_threshold", nullable = false)
    var alertThreshold: Int = 50,

    @Column(name = "target_url", nullable = false)
    var targetUrl: String = "http://backend:8080"
) {
    @Id
    var id: Int = 1 // Всегда 1
}