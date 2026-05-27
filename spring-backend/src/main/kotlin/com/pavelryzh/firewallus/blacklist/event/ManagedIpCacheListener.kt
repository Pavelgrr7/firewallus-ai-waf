package com.pavelryzh.firewallus.blacklist.event

import org.springframework.transaction.event.TransactionalEventListener

import com.pavelryzh.firewallus.blacklist.port.ManagedIpCache
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase

@Component
class ManagedIpCacheListener(
    private val managedIpCache: ManagedIpCache
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onIpAdded(event: ManagedIpEvent.Added) {
        managedIpCache.addIp(event.ipAddress, event.listType)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onIpRemoved(event: ManagedIpEvent.Removed) {
        managedIpCache.removeIp(event.ipAddress, event.listType)
    }
}