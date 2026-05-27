package com.pavelryzh.firewallus.blacklist.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

enum class IpListType { BLACKLIST, WHITELIST }

@Entity
@Table(name = "ip_lists")
class ManagedIp(
    @Column(name = "ip_address", nullable = false, unique = true)
    var ipAddress: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "list_type", nullable = false)
    var listType: IpListType,

    @Column(name = "description")
    var description: String? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var timestamp: Instant? = null
}