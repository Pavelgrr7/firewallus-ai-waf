package main.kotlin.com.pavelryzh.firewallus

@JvmInline
value class IpAddress(val value: String) {

    init {
        require(isValidIp(value)) { "Invalid IP address format: $value" }
    }

    companion object {
        // todo IPv6
        private val IPV4_REGEX = Regex(
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        )

        private fun isValidIp(ip: String): Boolean {
            return IPV4_REGEX.matches(ip)
        }

        fun parseOrNull(ip: String?): IpAddress? {
            if (ip.isNullOrBlank() || !isValidIp(ip)) return null
            return IpAddress(ip)
        }
    }
}