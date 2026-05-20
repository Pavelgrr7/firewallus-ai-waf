package com.pavelryzh.core

import java.net.URLDecoder


fun normalizePayload(input: String): String {
    var normalized = input

    normalized = runCatching { URLDecoder.decode(normalized, "UTF-8") }.getOrDefault(normalized)

    normalized = normalized.replace(Regex("\\s+"), " ")

    normalized = normalized.replace("/./", "/").replace("/../", "/")

    return normalized.lowercase()
}