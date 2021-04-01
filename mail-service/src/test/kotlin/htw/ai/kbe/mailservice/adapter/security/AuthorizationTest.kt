package htw.ai.kbe.mailservice.adapter.security

import org.apache.http.HttpHeaders
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

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
        mvc.get("/mail")
            .andExpect {
                status { isUnauthorized() }
                header { exists(HttpHeaders.WWW_AUTHENTICATE) }
            }
    }

    @WithMockUser
    @Test
    internal fun `should respond normally when authorized`() {
        mvc.post("/mail") {
            contentType = MediaType.APPLICATION_JSON
            content = "invalid request"
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
