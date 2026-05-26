package com.pavelryzh.firewallus.audit.service

import com.pavelryzh.firewallus.audit.domain.AuditLog
import com.pavelryzh.firewallus.audit.port.AuditLogRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuditLogService(private val auditRepo: AuditLogRepository){
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<AuditLog> {
        return auditRepo.findAll(pageable)
    }
}