package com.pavelryzh.service

import com.pavelryzh.model.GlobalSettings
import com.pavelryzh.model.WafRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WafConfigManager(
    private val redisWafClient: RedisWafClient
) : AutoCloseable {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _settings = MutableStateFlow(GlobalSettings())
    val settings: StateFlow<GlobalSettings> = _settings

    private val _activeRules = MutableStateFlow<List<WafRule>>(emptyList())
    val activeRules: StateFlow<List<WafRule>> = _activeRules

    init {
        scope.launch {
            while (isActive) {
                _settings.value = runCatching { redisWafClient.getSettings() }.getOrNull() ?: _settings.value
                _activeRules.value = runCatching { redisWafClient.getActiveRules() }.getOrDefault(_activeRules.value)
                delay(10_000)
            }
        }
    }

    override fun close() {
        scope.cancel()
    }
}