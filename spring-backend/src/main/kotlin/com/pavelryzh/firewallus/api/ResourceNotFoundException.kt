package com.pavelryzh.firewallus.api

open class ResourceNotFoundException(override val message: String) : RuntimeException() {
}