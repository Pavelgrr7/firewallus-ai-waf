package com.pavelryzh.model

data class ClientIdentity(
    val ip: String,
    val fingerprint: String,
    val jwtHash: String?
)