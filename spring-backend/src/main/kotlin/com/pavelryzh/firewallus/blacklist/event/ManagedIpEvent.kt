package com.pavelryzh.firewallus.blacklist.event

import com.pavelryzh.firewallus.blacklist.domain.IpListType
import com.pavelryzh.firewallus.blacklist.domain.ManagedIp
import java.util.UUID

sealed class ManagedIpEvent {
    data class Added(
        val ipAddress: String,
        val listType: IpListType,
        val adminId: UUID?,
    ) : ManagedIpEvent() {
        constructor(entity: ManagedIp, adminId: UUID?) : this(entity.ipAddress, entity.listType, adminId)
    }

    data class Removed(
        val ipAddress: String,
        val listType: IpListType,
        val adminId: UUID?,
    ) : ManagedIpEvent()
}