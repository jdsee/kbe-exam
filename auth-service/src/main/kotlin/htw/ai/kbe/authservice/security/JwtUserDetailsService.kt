package htw.ai.kbe.authservice.security

import htw.ai.kbe.authservice.model.UserCredentials
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class JwtUserDetailsService(
    private val userCredentialsRepository: UserCredentialsRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String) =
        userCredentialsRepository.findByUsername(username)
            ?.toUser()
            ?: throw AuthenticationServiceException("Username is no known.")

}

private fun UserCredentials.toUser() = User(this.username, this.password, emptyList())
