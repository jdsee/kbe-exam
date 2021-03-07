package htw.ai.kbe.kbegateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableEurekaClient
class KbeGatewayApplication {
    @Bean
    fun gateway(builder: RouteLocatorBuilder): RouteLocator = builder.routes()
        .route("test") { r -> r.path("/test").uri("http://test.com") }
        .build()
}

fun main(args: Array<String>) {
    runApplication<KbeGatewayApplication>(*args)
}


