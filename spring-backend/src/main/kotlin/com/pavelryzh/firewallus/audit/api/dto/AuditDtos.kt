package com.pavelryzh.firewallus.audit.api.dto

import com.pavelryzh.firewallus.audit.domain.AuditAction
import com.pavelryzh.firewallus.audit.domain.AuditLog
import java.time.Instant

data class AuditLogResponseDto(
    val id: String,
    val adminId: String?,
    val action: AuditAction,
    val ruleId: Int?,
    val ruleName: String,
    val timestamp: Instant?
)

fun AuditLog.toDto() = AuditLogResponseDto(
    id = this.id.toString(),
    adminId = this.adminId?.toString(),
    action = this.action,
    ruleId = this.ruleId,
    ruleName = this.ruleName,
    timestamp = this.timestamp
)