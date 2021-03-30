package htw.ai.kbe.authservice.security

import htw.ai.kbe.authservice.model.UserCredentials
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface UserCredentialsRepository: MongoRepository<UserCredentials, String> {
    fun findByUsername(username: String): UserCredentials?
}
