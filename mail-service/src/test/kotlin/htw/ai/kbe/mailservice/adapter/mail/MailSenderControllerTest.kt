package htw.ai.kbe.mailservice.adapter.mail

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import javax.ws.rs.core.Response.noContent

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class MailSenderControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var mockMailSender: JavaMailSender

    @Test
    fun `should send mail to given target and respond with no content`() {
        mvc.post("/mail") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                { "targetEmail": "test@mail.com", "subject": "new mail", "text": "test"}
            """.trimIndent()
        }.andExpect {
            status { noContent() }
            verify(mockMailSender).send(any(SimpleMailMessage::class.java))
        }
    }
}
