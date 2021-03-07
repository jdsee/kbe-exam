package htw.ai.kbe.songservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class SongServiceApplication

fun main(args: Array<String>) {
	runApplication<SongServiceApplication>(*args)
}
