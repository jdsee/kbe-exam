package htw.ai.kbe.songservice.adapter.api

import com.google.common.base.Charsets
import htw.ai.kbe.songservice.domain.model.Playlist
import htw.ai.kbe.songservice.domain.model.PlaylistRepository
import htw.ai.kbe.songservice.domain.model.Song
import htw.ai.kbe.songservice.domain.model.SongRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional


// Test-Data
private const val AUTHORIZED_USER = "authorizaed-user"
private const val UNAUTHORIZED_USER = "authorizaed-user"
private const val PLAYLISTS_PATH = "/playlists"
private const val USER_ID_PARAM_NAME = "userId"

/**
 * @author jslg
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@WithMockUser(username = AUTHORIZED_USER)
@AutoConfigureJsonTesters
class PlaylistControllerTest
@Autowired constructor(
    private val mvc: MockMvc,
    private val jsonPlaylist: JacksonTester<Playlist>,
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
) {
    @Disabled
    @ParameterizedTest
    @CsvSource(value = ["application/json,json", "application/xml,xml", "*/*,json"])
    fun `GET should support json and xml responses`(accept: String, expectedType: String) {
        createTestPlaylist(AUTHORIZED_USER, true)
        val responseBody = mvc.get(PLAYLISTS_PATH) {
            param("userId", AUTHORIZED_USER)
            accept(MediaType.valueOf(accept))
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.valueOf("application/$expectedType")) }
        }.andReturn().response.getContentAsString(Charsets.UTF_8)
        assertThat(responseBody).startsWith(if (expectedType == "json") "[" else "<")
    }

    private fun createTestPlaylist(ownerId: String, personal: Boolean) = playlistRepository.save(
        Playlist(
            name = "the playlist",
            personal = personal,
            ownerId = ownerId,
            songs = mutableListOf(
                Song(
                    id = 100,
                    title = "best song ever",
                    artist = "best artist ever",
                    album = "best album ever",
                    released = 2042
                )
            )
        )
    )
}
