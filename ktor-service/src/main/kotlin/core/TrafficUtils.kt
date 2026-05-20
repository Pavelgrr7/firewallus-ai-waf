package com.pavelryzh.core

import java.net.URLDecoder


fun normalizePayload(input: String): String {
    var normalized = input
    var prev: String
    do {
        prev = normalized
        normalized = runCatching { URLDecoder.decode(normalized, "UTF-8") }.getOrDefault(normalized)
    } while (normalized != prev)

    normalized = normalized.replace(Regex("\\s+"), " ")
    normalized = normalized.replace("/./", "/").replace("/../", "/")
    return normalized.lowercase()
}