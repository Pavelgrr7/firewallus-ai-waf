package com.pavelryzh.firewallus.blacklist.port

import com.pavelryzh.firewallus.blacklist.domain.IpListType

interface ManagedIpCache {
    fun addIp(ipAddress: String, listType: IpListType)
    fun removeIp(ipAddress: String, listType: IpListType)
}