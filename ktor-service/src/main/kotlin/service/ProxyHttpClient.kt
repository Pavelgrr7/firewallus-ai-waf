package com.pavelryzh.service

import com.pavelryzh.model.TargetUrl
import io.ktor.server.application.ApplicationCall

interface ProxyHttpClient {
    suspend fun proxyToBackend(targetUrl: TargetUrl, call: ApplicationCall, cachedBodyBytes: ByteArray?)
}