package htw.ai.kbe.authservice.security

import com.google.common.base.Charsets
import htwb.ai.jolu.adapter.security.config.SecurityConfiguration.AUTH_PATH
import htwb.ai.jolu.domain.model.UserAccount
import htwb.ai.jolu.domain.repository.UserAccountRepository
import htwb.ai.jolu.testutil.TransactionalIntegrationTest
import lombok.RequiredArgsConstructor
import org.apache.http.HttpHeaders
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

/**
 * @author jslg
 */
@TransactionalIntegrationTest
@RequiredArgsConstructor(onConstructor = __(Autowired))
class AuthenticationTest {
    private val mvc: MockMvc? = null
    private val userRepository: UserAccountRepository? = null
    private val passwordEncoder: PasswordEncoder? = null

    @BeforeEach
    fun setUp() {
        userRepository.save(
            UserAccount.builder()
                .userId(USER_ID)
                .password(passwordEncoder!!.encode(USER_PASSWORD)).build()
        )
    }

    @Test
    @Throws(Exception::class)
    fun shouldGetForbiddenOnSecuredEndpointWithoutAuth() {
        mvc!!.perform(MockMvcRequestBuilders.get(SONGS_PATH))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(
                MockMvcResultMatchers.header()
                    .exists(HttpHeaders.WWW_AUTHENTICATE)
            )
            .andReturn()
    }

    @Test
    @Throws(Exception::class)
    fun shouldBeAbleToLoginWithRegisteredUser() {
        doAuthenticationRequest(USER_ID, USER_PASSWORD)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo { result: MvcResult ->
                Assertions.assertThat(
                    result.response.contentAsString
                ).startsWith("Bearer")
            }
    }

    @Test
    @Throws(Exception::class)
    fun shouldGetUnauthorizedForUnregisteredUser() {
        doAuthenticationRequest(USER_ID, INVALID_PASSWORD)
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andReturn()
    }

    @Test
    @Throws(Exception::class)
    fun shouldBeAbleToAccessSecuredEndpointWithAuth() {
        val token =
            doAuthenticationRequest(USER_ID, USER_PASSWORD)
                .andReturn().response
                .getContentAsString(Charsets.UTF_8).replace("\\n".toRegex(), "")
        mvc!!.perform(
            MockMvcRequestBuilders.get(SONGS_PATH)
                .header(HttpHeaders.AUTHORIZATION, token)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo { result: MvcResult ->
                Assertions.assertThat(
                    result.response.contentAsString
                ).isNotEmpty()
            }
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturnDifferentTokensForDifferentUsers() {
        userRepository.save(
            UserAccount.builder()
                .userId(OTHER_USER_ID)
                .password(passwordEncoder!!.encode(USER_PASSWORD)).build()
        )
        val token1 =
            doAuthenticationRequest(USER_ID, USER_PASSWORD)
                .andReturn().response.contentAsString
        val token2 = doAuthenticationRequest(
            OTHER_USER_ID,
            USER_PASSWORD
        )
            .andReturn().response.contentAsString
        Assertions.assertThat(token1).startsWith("Bearer")
        Assertions.assertThat(token2).startsWith("Bearer")
        Assertions.assertThat(token1).isNotEqualTo(token2)
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturnUnauthorizedAndEmptyBodyForUnauthorizedUser() {
        doAuthenticationRequest(USER_ID, INVALID_PASSWORD)
            .andDo { result: MvcResult ->
                Assertions.assertThat(result.response.status)
                    .isEqualTo(HttpStatus.UNAUTHORIZED.value())
                Assertions.assertThat(result.response.contentAsString).isNullOrEmpty()
            }
    }

    @Throws(Exception::class)
    private fun doAuthenticationRequest(
        userId: String,
        password: String
    ): ResultActions {
        return mvc!!.perform(
            post(AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"$userId\",\"password\":\"$password\"}")
        )
    }

    companion object {
        private const val SONGS_PATH = "/songs"
        const val USER_ID = "mmuster"
        const val USER_PASSWORD = "pass1234"
        private const val OTHER_USER_ID = "other"
        private const val INVALID_PASSWORD = "wrong"
    }
}
