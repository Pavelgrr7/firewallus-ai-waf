package com.pavelryzh.firewallus.audit.event


import com.pavelryzh.firewallus.audit.domain.AuditAction
import com.pavelryzh.firewallus.audit.domain.AuditLog
import com.pavelryzh.firewallus.audit.service.AuditLogService
import com.pavelryzh.firewallus.rule.event.RuleCacheEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class AuditLogListener(
    private val auditLogService: AuditLogService
) {
    @Async
    @EventListener
    fun onRuleChanged(event: RuleCacheEvent) {

        val auditLog = when (event) {
            is RuleCacheEvent.Saved -> {

                AuditLog(
                    adminId = event.adminId,
                    action = if (event.isActive) AuditAction.ENABLE_RULE else AuditAction.DISABLE_RULE,
                    ruleId = event.ruleId,
                    ruleName = event.name
                )
            }
            is RuleCacheEvent.Deleted -> {
                AuditLog(
                    adminId = event.adminId,
                    action = AuditAction.DELETE_RULE,
                    ruleId = event.ruleId,
                    ruleName = "Deleted Rule #${event.ruleId}"
                )
            }
        }

        auditLogService.save(auditLog)
    }
}