package com.pavelryzh.service

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveChannel
import io.ktor.server.request.uri

import io.ktor.client.HttpClient
import io.ktor.client.request.request

import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.server.response.header
import io.ktor.server.response.respond

const val BACKEND_TARGET_URL = "http://backend-spring:8080"

class KtorHttpClient(
    private val httpClient: HttpClient,
): AutoCloseable, ProxyHttpClient {

    override suspend fun proxyToBackend(call: ApplicationCall, cachedBodyBytes: ByteArray?) {

        val backendResponse = httpClient.request("$BACKEND_TARGET_URL${call.request.uri}") {
            method = call.request.httpMethod
            headers {
                call.request.headers.forEach { key, values ->
                    if (!key.equals(HttpHeaders.Host, ignoreCase = true) &&
                        !key.equals(HttpHeaders.ContentLength, ignoreCase = true) &&
                        !key.equals(HttpHeaders.TransferEncoding, ignoreCase = true)
                    ) {
                        appendAll(key, values)
                    }
                }
            }

            if (cachedBodyBytes != null) {
                setBody(cachedBodyBytes)
            } else {
                setBody(call.receiveChannel())
            }
        }

        backendResponse.headers.forEach { key, values ->
            if (!key.equals(HttpHeaders.ContentType, ignoreCase = true) &&
                !key.equals(HttpHeaders.ContentLength, ignoreCase = true) &&
                !key.equals(HttpHeaders.TransferEncoding, ignoreCase = true)
            ) {
                values.forEach { value ->
                    call.response.header(key, value)
                }
            }
        }

        val bodyChannel = backendResponse.bodyAsChannel()
        call.respond(
            status = backendResponse.status,
            message = object : OutgoingContent.ReadChannelContent() {
                override val contentType = backendResponse.contentType()
                override val contentLength = backendResponse.contentLength()
                override fun readFrom() = bodyChannel
            }
        )
    }

    override fun close() {
        httpClient.close()
    }
}