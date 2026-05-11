package com.pavelryzh.firewallus.rule.event

import com.pavelryzh.firewallus.rule.port.RuleCache
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.transaction.event.TransactionPhase

@Component
class RuleCacheUpdater(private val cache: RuleCache) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onRuleCacheEvent(event: RuleCacheEvent) {

        when (event) {
            is RuleCacheEvent.Saved -> {
                if (event.isActive) {
                    cache.saveRule(event)
                } else {
                    cache.deleteRule(event.ruleId)
                }
            }
            is RuleCacheEvent.Deleted -> {
                cache.deleteRule(event.ruleId)
            }
        }
    }
}