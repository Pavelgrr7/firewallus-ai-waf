package com.pavelryzh.firewallus.audit.api


import com.pavelryzh.firewallus.audit.api.dto.AuditLogResponseDto
import com.pavelryzh.firewallus.audit.api.dto.toDto
import com.pavelryzh.firewallus.audit.service.AuditLogService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
class AuditLogController(
    private val auditLogService: AuditLogService
) {
    @GetMapping
    fun getAuditLogs(
        @RequestParam(required = false) search: String?,
        @PageableDefault(size = 20, page = 0, sort = ["timestamp"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): Page<AuditLogResponseDto> {
        return auditLogService.findAll(search, pageable).map { it.toDto() }
    }
}