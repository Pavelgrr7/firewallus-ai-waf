package com.pavelryzh.service

import io.ktor.server.application.ApplicationCall

interface ProxyHttpClient {
    suspend fun proxyToBackend(call: ApplicationCall, cachedBodyBytes: ByteArray?)
}