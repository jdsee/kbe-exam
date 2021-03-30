package htw.ai.kbe.kbegateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class KbeGatewayApplication
//{
//    @Bean
//    fun gateway(builder: RouteLocatorBuilder): RouteLocator = builder.routes()
//        .route("songs") { r ->
//            r.path("/songs/**")
//                .uri("lb://SONG-SERVICE")
//        }
//        .route("playlists") { r ->
//            r.path("/playlists/**")
//                .uri("lb://PLAYLIST-SERVICE")
//        }
//        .route("users") { r ->
//            r.path("/users/**")
//                .uri("lb://USERS-SERVICE")
//        }
//        .route("auth") { r ->
//            r.path("/auth")
//                .uri("lb://AUTH-SERVICE")
//        }
//        .build()
//}

fun main(args: Array<String>) {
    runApplication<KbeGatewayApplication>(*args)
}


