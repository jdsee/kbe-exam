package htw.ai.kbe.authservice.security

import htw.ai.kbe.authservice.domain.model.UserCredentials
import htw.ai.kbe.authservice.domain.model.UserCredentialsRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

/**
 * @author jslg
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
internal class AuthenticationTest
@Autowired constructor(
    private val mvc: MockMvc,
    private val userRepository: UserCredentialsRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @BeforeEach
    internal fun setUp() {
        userRepository.save(
            UserCredentials(
                username = USER_ID,
                password = passwordEncoder.encode(USER_PASSWORD)
            )
        )
    }

    private fun doAuthenticationRequest(
        username: String,
        password: String
    ) = mvc.post(AUTH_PATH) {
        contentType = MediaType.APPLICATION_JSON
        content = """{"username":"$username","password":"$password"}"""
    }

    @Test
    internal fun shouldBeAbleToLoginWithRegisteredUser() {
        val response = doAuthenticationRequest(USER_ID, USER_PASSWORD)
            .andExpect {
                status { isOk() }
            }.andReturn().response
        assertThat(response.contentAsString).startsWith("Bearer")
    }

    @Test
    internal fun shouldGetUnauthorizedForUnregisteredUser() {
        doAuthenticationRequest(USER_ID, INVALID_PASSWORD)
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    internal fun shouldReturnDifferentTokensForDifferentUsers() {
        userRepository.save(
            UserCredentials(
                username = OTHER_USER_ID,
                password = passwordEncoder.encode(USER_PASSWORD)
            )
        )

        val token1 =
            doAuthenticationRequest(USER_ID, USER_PASSWORD)
                .andReturn().response.contentAsString
        val token2 = doAuthenticationRequest(OTHER_USER_ID, USER_PASSWORD)
            .andReturn().response.contentAsString

        assertThat(token1).startsWith("Bearer")
        assertThat(token2).startsWith("Bearer")
        assertThat(token1).isNotEqualTo(token2)
    }

    @Test
    internal fun shouldReturnUnauthorizedAndEmptyBodyForUnauthorizedUser() {
        val response = doAuthenticationRequest(USER_ID, INVALID_PASSWORD)
            .andExpect {
                status { isUnauthorized() }
            }.andReturn().response

        assertThat(response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
        assertThat(response.contentAsString).contains("Bad credentials")
    }

    companion object {
        const val USER_ID = "tester"
        const val USER_PASSWORD = "test123"
        private const val OTHER_USER_ID = "other"
        private const val INVALID_PASSWORD = "wrong"
    }
}
