package com.pavelryzh.firewallus.blacklist.service

import com.pavelryzh.firewallus.blacklist.domain.IpListType
import com.pavelryzh.firewallus.blacklist.domain.ManagedIp
import com.pavelryzh.firewallus.blacklist.event.ManagedIpEvent
import com.pavelryzh.firewallus.blacklist.port.ManagedIpRepository
import com.pavelryzh.firewallus.infra.security.CurrentAdminProvider
import com.pavelryzh.firewallus.rule.domain.IpAddress
import jakarta.persistence.EntityNotFoundException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ManagedIpService(
    private val ipRepo: ManagedIpRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val currentAdminProvider: CurrentAdminProvider,
) {
    @Transactional(readOnly = true)
    fun getIps(
        listType: IpListType?,
        pageable: Pageable,
    ): Page<ManagedIp> =
        if (listType != null) {
            ipRepo.findByListType(listType, pageable)
        } else {
            ipRepo.findAll(pageable)
        }

    @Transactional
    fun addIp(
        ipAddress: String,
        type: IpListType,
        description: String?,
    ): ManagedIp {
        val validIp = IpAddress(ipAddress)

        ipRepo.findByIpAddress(validIp.value)?.let { existingIp ->
            throw IllegalArgumentException("IP ${validIp.value} уже находится в ${existingIp.listType}")
        }

        val newIp =
            ManagedIp(
                ipAddress = validIp.value,
                listType = type,
                description = description,
            )
        val saved = ipRepo.save(newIp)

        val adminId = currentAdminProvider.getCurrentAdminId()

        // Кидаем событие, чтобы Redis добавил этот IP в кэш!
        eventPublisher.publishEvent(ManagedIpEvent.Added(saved, adminId))

        return saved
    }

    @Transactional
    fun removeIpById(id: UUID) {
        val adminId = currentAdminProvider.getCurrentAdminId()

        val ip =
            ipRepo.findById(id).orElseThrow {
                EntityNotFoundException("ManagedIp not found: $id")
            }

        ipRepo.deleteById(id)
        eventPublisher.publishEvent(ManagedIpEvent.Removed(ip.ipAddress, ip.listType, adminId))
    }

    @Transactional
    fun removeIpByAddress(ipAddress: String) {
        val adminId = currentAdminProvider.getCurrentAdminId()

        val ip = ipRepo.findByIpAddress(ipAddress) ?: throw EntityNotFoundException("ManagedIp not found: $ipAddress")

        ipRepo.delete(ip)

        eventPublisher.publishEvent(
            ManagedIpEvent.Removed(
                ipAddress = ip.ipAddress,
                listType = ip.listType,
                adminId = adminId,
            ),
        )
    }
}
