import io.gatling.javaapi.core.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*

class WafAttackSimulation : Simulation() {

    private val httpProtocol = http
        .baseUrl("http://localhost:80")
        .acceptHeader("application/json")

    // Обычный трафик (Легитимные пользователи)
    private val normalTraffic = scenario("Normal Users")
        .exec(http("Get Home Page").get("/"))
        .pause(1)
        .exec(http("Get API").get("/api/data"))

    // Атака SQL-инъекцией (Проверка Rule Engine)
    private val sqlInjectionAttack = scenario("SQLi Attackers")
        .exec(
            http("Malicious SQLi")
                .get("/api/users?id=1%20OR%201=1")
                .header("User-Agent", "sqlmap/1.5")
                // Мы ожидаем, что WAF нас заблокирует (403), поэтому говорим Gatling, что 403 - это УСПЕХ теста
                .check(status().`is`(403))
        )

    // Брутфорс / DDoS
    private val rateLimitAttack = scenario("Spammers")
        // Исчерпание лимита
        .repeat(100).on(
            exec(
                http("Fill Bucket")
                    .get("/login")
                    .check(status().`in`(200, 404, 429))
            )
        )
        // Проверка блокировки
        .exec(
            http("Rate Limit Blocked")
                .get("/login")
                .check(status().`is`(429))
        )

    init {
        setUp(
            // 50 обычных юзеров плавно заходят в течение 10 секунд
            normalTraffic.injectOpen(rampUsers(50).during(10)),

            // 20 хакеров бьют инъекциями
            sqlInjectionAttack.injectOpen(atOnceUsers(20)),

            // 1 спамер пытается положить сайт
            rateLimitAttack.injectOpen(atOnceUsers(1))
        ).protocols(httpProtocol)
    }
}