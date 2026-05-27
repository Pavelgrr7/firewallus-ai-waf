package com.pavelryzh.firewallus.blacklist.service

import com.pavelryzh.firewallus.blacklist.domain.IpListType
import com.pavelryzh.firewallus.blacklist.domain.ManagedIp
import com.pavelryzh.firewallus.blacklist.event.ManagedIpEvent
import com.pavelryzh.firewallus.blacklist.port.ManagedIpRepository
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
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional(readOnly = true)
    fun getIps(listType: IpListType?, pageable: Pageable): Page<ManagedIp> {
        return if (listType != null) {
            ipRepo.findByListType(listType, pageable)
        } else {
            ipRepo.findAll(pageable)
        }
    }

    @Transactional
    fun addIp(ipAddress: String, type: IpListType, description: String?): ManagedIp {
        val validIp = IpAddress(ipAddress)

        ipRepo.findByIpAddress(validIp.value)?.let { existingIp ->
            throw IllegalArgumentException("IP ${validIp.value} уже находится в ${existingIp.listType}")
        }

        val newIp = ManagedIp(
            ipAddress = ipAddress,
            listType = type,
            description = description
        )
        val saved = ipRepo.save(newIp)

        // Кидаем событие, чтобы Redis добавил этот IP в кэш!
        eventPublisher.publishEvent(ManagedIpEvent.Added(saved))

        return saved
    }

    @Transactional
    fun removeIpById(id: UUID) {
        val ip = ipRepo.findById(id).orElseThrow {
            EntityNotFoundException("ManagedIp not found: $id")
        }
        ipRepo.deleteById(id)
        eventPublisher.publishEvent(ManagedIpEvent.Removed(ip.ipAddress, ip.listType))
    }
}