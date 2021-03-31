package htw.ai.kbe.authservice.domain.model

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface UserCredentialsRepository: MongoRepository<UserCredentials, String> {
    fun findByUsername(username: String): UserCredentials?
}
