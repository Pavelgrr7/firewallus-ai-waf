package com.pavelryzh.service

import com.pavelryzh.model.TargetUrl
import io.ktor.client.*
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.http.headers
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class KtorHttpClient(
    private val httpClient: HttpClient,
): AutoCloseable, ProxyHttpClient {

    override suspend fun proxyToBackend(targetUrl: TargetUrl, call: ApplicationCall, cachedBodyBytes: ByteArray?) {

        val backendResponse = httpClient.request("${targetUrl.normalized}${call.request.uri}") {
            method = call.request.httpMethod
            headers {
                call.request.headers.forEach { key, values ->
                    if (!key.equals(HttpHeaders.Host, ignoreCase = true) &&
                        !key.equals(HttpHeaders.ContentLength, ignoreCase = true) &&
                        !key.equals(HttpHeaders.TransferEncoding, ignoreCase = true) &&
                        !key.equals(HttpHeaders.Connection, ignoreCase = true)
                    ) {
                        appendAll(key, values)
                    }
                }
            }

            if (cachedBodyBytes != null) {
                setBody(cachedBodyBytes)
            } else {
                val contentLength = call.request.headers[HttpHeaders.ContentLength]?.toLongOrNull()
                val isChunked = call.request.headers[HttpHeaders.TransferEncoding]?.equals("chunked", ignoreCase = true) == true
                if ((contentLength != null && contentLength > 0) || isChunked) {
                    setBody(call.receiveChannel())
                }
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