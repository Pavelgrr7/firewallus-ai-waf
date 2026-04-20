import com.pavelryzh.plugins.configureDI
import com.pavelryzh.plugins.configureHTTP
import com.pavelryzh.plugins.configureKafka
import com.pavelryzh.plugins.configureSerialization
import com.pavelryzh.kafka.KafkaTrafficProducer
import com.pavelryzh.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import org.koin.ktor.ext.inject
import plugins.configureDatabases

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDatabases()
    configureSerialization()
    configureHTTP()
    configureKafka()
    configureDI()
    configureRouting()

    monitor.subscribe(ApplicationStopped) {
        val kafkaProducer by inject<KafkaTrafficProducer>()
        kafkaProducer.close()
        log.info("KafkaTrafficProducer closed successfully.")
    }
}
