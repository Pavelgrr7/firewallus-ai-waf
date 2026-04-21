import kotlin.text.trimIndent


val jsonPayload = """
{
    "timestamp": 1712213456789,
    "request_id": "uuid-v4",
    "session_id": "hash(IP + User-Agent)",
    "ip": "192.168.1.55",
    "method": "POST",
    "path": "/api/v1/login",
    "query_params": "?debug=true&user=admin",
    "headers": {
    "user-agent": "Mozilla/5.0...",
    "content-type": "application/json",
    "accept": "*/*"
},
    "body_size": 2048,
    "body_truncated": false,
    "body_snippet": "{\"username\": \"admin' OR 1=1 --\", \"password\": \"123\"}"
}
""".trimIndent()
