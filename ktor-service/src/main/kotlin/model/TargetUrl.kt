package com.pavelryzh.model

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class TargetUrl(val rawValue: String) {

    init {
        require(rawValue.trim().startsWith("http://") || rawValue.trim().startsWith("https://")) {
            "Target URL must start with http:// or https://"
        }
    }

    // Вычисляемое свойство: само уберет пробелы по краям и отрежет слеш на конце!
    val normalized: String
        get() = rawValue.trim().removeSuffix("/")
}