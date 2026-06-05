package com.pavelryzh.firewallus.audit.event


import com.pavelryzh.firewallus.audit.domain.AuditAction
import com.pavelryzh.firewallus.audit.domain.AuditLog
import com.pavelryzh.firewallus.audit.service.AuditLogService
import com.pavelryzh.firewallus.blacklist.event.ManagedIpEvent
import com.pavelryzh.firewallus.rule.event.RuleCacheEvent
import com.pavelryzh.firewallus.settings.event.SettingsUpdatedEvent
import com.pavelryzh.firewallus.user.event.AdminLoginEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class AuditLogListener(
    private val auditLogService: AuditLogService
) {
    @Async
    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT,
        fallbackExecution = true
    )
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
                    ruleId = null,
                    ruleName = "Deleted Rule #${event.ruleId}"
                )
            }
        }

        auditLogService.save(auditLog)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun onSettingsChanged(event: SettingsUpdatedEvent) {
        val auditLog = AuditLog(
            adminId = event.adminId,
            action = AuditAction.UPDATE_SETTINGS,
            ruleId = null,
            ruleName = "Global WAF Settings" // TODO: использовать общие/базовые поля, не привязанные к домену правил
        )
        auditLogService.save(auditLog)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun onAdminLogin(event: AdminLoginEvent) {
        val auditLog = AuditLog(
            adminId = event.adminId,
            action = AuditAction.LOGIN,
            ruleId = null,
            ruleName = "User: ${event.username}"
        )
        auditLogService.save(auditLog)
    }
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun onManagedIpChanged(event: ManagedIpEvent) {

        val auditLog = when (event) {
            is ManagedIpEvent.Added -> AuditLog(
                adminId = event.adminId,
                action = AuditAction.ADD_MANAGED_IP,
                ruleId = null,
                ruleName = "Added ${event.ipAddress} to ${event.listType}"
            )
            is ManagedIpEvent.Removed -> AuditLog(
                adminId = event.adminId,
                action = AuditAction.REMOVE_MANAGED_IP,
                ruleId = null,
                ruleName = "Removed ${event.ipAddress} from ${event.listType}"
            )
        }
        auditLogService.save(auditLog)
    }
}