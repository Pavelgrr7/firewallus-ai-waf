package com.pavelryzh.firewallus.audit.port

import com.pavelryzh.firewallus.audit.domain.AuditLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, UUID> {

    @Query("""
        SELECT a FROM AuditLog a 
        WHERE :search IS NULL OR :search = '' 
           OR LOWER(a.ruleName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(CAST(a.action AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(CAST(a.adminId AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    fun searchLogs(search: String?, pageable: Pageable): Page<AuditLog>
}