import io.gatling.javaapi.core.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*
import kotlin.random.Random

class WafAttackSimulation : Simulation() {

    private val httpProtocol = http
        .baseUrl("http://127.0.0.1:80")
        .acceptHeader("application/json")

    val randomIpFeeder = generateSequence {
        mapOf(
            "randomIp" to "${Random.nextInt(1, 255)}.${Random.nextInt(0, 255)}.${Random.nextInt(0, 255)}.${Random.nextInt(1, 255)}"
        )
    }.iterator()

    // Обычный трафик (Легитимные пользователи)
    private val normalTraffic = scenario("Normal Users")
        .feed(randomIpFeeder)
        .exec(http("Get Home Page").get("/").header("X-Forwarded-For", "#{randomIp}"))
        .pause(1)
        .exec(http("Get API").get("/api/data").header("X-Forwarded-For", "#{randomIp}"))

    // Атака SQL-инъекцией (Проверка Rule Engine)
    private val sqlInjectionAttack = scenario("SQLi Attackers")
        .feed(randomIpFeeder)
        .exec(
            http("Malicious SQLi")
                .get("/api/users?id=1%20OR%201=1").header("X-Forwarded-For", "#{randomIp}")
                .header("User-Agent", "sqlmap/1.5")
                // Мы ожидаем, что WAF нас заблокирует (403), поэтому говорим Gatling, что 403 - это УСПЕХ теста
                .check(status().`is`(403))
        )

    // Брутфорс / DDoS
    private val rateLimitAttack = scenario("Spammers")
        .feed(randomIpFeeder)
        // Исчерпание лимита
        .repeat(100).on(
            exec(
                http("Fill Bucket")
                    .get("/login").header("X-Forwarded-For", "#{randomIp}")
                    .check(status().`in`(200, 404, 429))
            )
        )
        // Проверка блокировки
        .exec(
            http("Rate Limit Blocked")
                .get("/login").header("X-Forwarded-For", "#{randomIp}")
                .check(status().`is`(429))
        )


    init {
        setUp(
            // 50 обычных юзеров плавно заходят в течение 10 секунд
            normalTraffic.injectOpen(nothingFor(5), rampUsers(5000).during(1)),
            // 20 хакеров бьют инъекциями
            sqlInjectionAttack.injectOpen(nothingFor(5), atOnceUsers(20)),

            // 1 спамер пытается положить сайт
            rateLimitAttack.injectOpen(nothingFor(5), atOnceUsers(1))
        ).protocols(httpProtocol)
    }
    init {
        setUp(
            // Сценарий "Легитимные пользователи"
            normalTraffic.injectOpen(
                // Плавно поднимаем нагрузку от 10 запросов в секунду до 500 запросов в секунду в течение 2 минут!
                rampUsersPerSec(10.0).to(500.0).during(120)
            )
        ).protocols(httpProtocol)
    }
}