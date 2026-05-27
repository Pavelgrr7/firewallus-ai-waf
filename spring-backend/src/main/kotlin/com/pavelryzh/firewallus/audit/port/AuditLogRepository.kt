package com.pavelryzh.firewallus.audit.port

import com.pavelryzh.firewallus.audit.domain.AuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, UUID> {
}