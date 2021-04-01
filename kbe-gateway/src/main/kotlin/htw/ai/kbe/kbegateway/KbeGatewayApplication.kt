package htw.ai.kbe.kbegateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class KbeGatewayApplication

fun main(args: Array<String>) {
    runApplication<KbeGatewayApplication>(*args)
}


