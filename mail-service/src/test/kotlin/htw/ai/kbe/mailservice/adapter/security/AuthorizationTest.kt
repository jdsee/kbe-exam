package htw.ai.kbe.songservice.adapter.security

import htw.ai.kbe.songservice.adapter.api.ApiEndpointConstants.SONGS_PATH
import org.apache.http.HttpHeaders
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationTest
@Autowired constructor(
    private val mvc: MockMvc
) {
    @Test
    fun `should return unauthorized if not logged in`() {
        mvc.get("/songs")
            .andExpect {
                status { isUnauthorized() }
                header { exists(HttpHeaders.WWW_AUTHENTICATE) }
            }
    }

    @WithMockUser
    @Test
    internal fun `should respond normally when authorized`() {
        mvc.get(SONGS_PATH).andExpect {
            status { isOk() }
        }
    }
}
