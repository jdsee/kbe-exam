package htw.ai.kbe.authservice

import htw.ai.kbe.authservice.domain.model.UserCredentials
import htw.ai.kbe.authservice.domain.model.UserCredentialsRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootApplication
class AuthServiceApplication {
    @Bean
    fun demoData(repo: UserCredentialsRepository, passwordEncoder: PasswordEncoder) = CommandLineRunner {
        repo.save(
            UserCredentials("mmuster", passwordEncoder.encode("pw1234"))
        )
    }
}

fun main(args: Array<String>) {
    runApplication<AuthServiceApplication>(*args)
}
