package com.pavelryzh.firewallus.blacklist.api

import com.pavelryzh.firewallus.blacklist.domain.IpListType
import com.pavelryzh.firewallus.blacklist.service.ManagedIpService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/access-control")
@PreAuthorize("hasRole('ADMIN')")
class ManagedIpController(private val managedIpService: ManagedIpService) {
    @GetMapping
    fun getAll(
        @PageableDefault(size = 20, page = 0) pageable: Pageable,
        @RequestParam(required = false) listType: IpListType?
    ): Page<ManagedIpResponseDto> {
        return managedIpService.getIps(listType, pageable).map { it.toDto() }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addIp(@Valid @RequestBody dto: CreateManagedIpDto): ManagedIpResponseDto {
        val saved = managedIpService.addIp(dto.ipAddress, dto.listType, dto.description)
        return saved.toDto()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeIp(@PathVariable id: UUID) {
        managedIpService.removeIpById(id)
    }
}
