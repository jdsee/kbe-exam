package htw.ai.kbe.songservice.adapter.api

import com.nhaarman.mockitokotlin2.argumentCaptor
import htw.ai.kbe.songservice.TestDataProvider
import htw.ai.kbe.songservice.domain.ports.SimpleMailRequest
import htw.ai.kbe.songservice.domain.ports.SimpleMailSenderClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "someone")
internal class SharePlaylistControllerTest
@Autowired constructor(
    private val testDataProvider: TestDataProvider,
    private val mvc: MockMvc
) {
    @MockBean
    private lateinit var mockMailSenderClient: SimpleMailSenderClient

    private val mailRequestCaptor = argumentCaptor<SimpleMailRequest>()

    @ParameterizedTest
    @CsvSource(
        "someone,true",
        "someone-else,false"
    )
    internal fun `should call mail-service when song exists and actual user has access`(
        ownerId: String, personalPlaylist: Boolean
    ) {
        val playlist = testDataProvider.createTestPlaylist(ownerId, personalPlaylist)
        postSharePlaylistRequest(playlist.id!!, "test@mail.com")
            .andExpect {
                status { isOk() }
                verify(mockMailSenderClient).sendMail(anyString(), mailRequestCaptor.capture())
                assertThat(mailRequestCaptor.firstValue.targetEmail).isEqualTo("test@mail.com")
                assertThat(mailRequestCaptor.firstValue.text).contains(playlist.name)
            }
    }

    @Test
    internal fun `should not call mail-service if song is not owned by actual user`() {
        val playlist = testDataProvider.createTestPlaylist("someone-else", true)
        postSharePlaylistRequest(playlist.id!!, "random@mail.com")
            .andExpect {
                status { isForbidden() }
                verifyNoInteractions(mockMailSenderClient)
            }.andReturn()
    }

    @Test
    internal fun `should not call mail-service if song does not exist`() {
        postSharePlaylistRequest(123, "random@mail.com")
            .andExpect {
                status { isNotFound() }
                verifyNoInteractions(mockMailSenderClient)
            }
    }

    private fun postSharePlaylistRequest(playlistId: Long, targetEmail: String) =
        mvc.post("/share-playlist") {
            header(HttpHeaders.AUTHORIZATION, "fake-token")
            contentType = MediaType.APPLICATION_JSON
            content = """
                    { "playlistId": ${playlistId}, "targetEmail": "$targetEmail" }
                """.trimIndent()
        }
}
