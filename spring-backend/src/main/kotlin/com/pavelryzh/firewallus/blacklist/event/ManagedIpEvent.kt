package com.pavelryzh.firewallus.blacklist.event

import com.pavelryzh.firewallus.blacklist.domain.IpListType
import com.pavelryzh.firewallus.blacklist.domain.ManagedIp

sealed class ManagedIpEvent {
    data class Added(
        val ipAddress: String,
        val listType: IpListType
    ) : ManagedIpEvent() {
        constructor(entity: ManagedIp) : this(entity.ipAddress, entity.listType)
    }

    data class Removed(
        val ipAddress: String,
        val listType: IpListType
    ) : ManagedIpEvent()
}