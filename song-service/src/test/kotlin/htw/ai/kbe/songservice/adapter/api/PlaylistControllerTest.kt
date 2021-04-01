package htw.ai.kbe.songservice.adapter.api

import com.google.common.base.Charsets
import htw.ai.kbe.songservice.TestDataProvider
import htw.ai.kbe.songservice.domain.model.Playlist
import htw.ai.kbe.songservice.domain.model.PlaylistRepository
import htw.ai.kbe.songservice.domain.model.Song
import htw.ai.kbe.songservice.domain.model.SongRepository
import htw.ai.kbe.songservice.testutils.parameterized.FileSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.boot.test.json.JsonbTester
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.*
import org.springframework.transaction.annotation.Transactional


// Test-Data
private const val AUTHORIZED_USER = "authorizaed-user"
private const val UNAUTHORIZED_USER = "authorized-user"
private const val PLAYLISTS_PATH = "/playlists"

/**
 * @author jslg
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@WithMockUser(username = AUTHORIZED_USER)
@AutoConfigureJsonTesters
internal class PlaylistControllerTest
@Autowired constructor(
    private val mvc: MockMvc,
    private val testDataProvider: TestDataProvider,
    private val playlistRepository: PlaylistRepository,
) {
    @Autowired
    private lateinit var jsonTester: JacksonTester<Playlist>

    @Disabled
    @ParameterizedTest
    @CsvSource(value = ["application/json,json", "application/xml,xml", "*/*,json"])
    internal fun `GET should support json and xml responses`(accept: String, expectedType: String) {
        testDataProvider.createTestPlaylist(AUTHORIZED_USER, true)
        val responseBody = mvc.get(PLAYLISTS_PATH) {
            param("userId", AUTHORIZED_USER)
            accept(MediaType.valueOf(accept))
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.valueOf("application/$expectedType")) }
        }.andReturn().response.getContentAsString(Charsets.UTF_8)
        assertThat(responseBody).startsWith(if (expectedType == "json") "[" else "<")
    }

    @Test
    fun `GET should return playlist with proper content`() {
        val playlist = testDataProvider.createTestPlaylist(AUTHORIZED_USER, false)
        val response = mvc.get("$PLAYLISTS_PATH/${playlist.id}")
            .andReturn().response.contentAsString

        assertThat(jsonTester.parseObject(response))
            .usingRecursiveComparison()
            .ignoringFields("owner")
            .isEqualTo(playlist)
    }


    @ParameterizedTest
    @CsvSource(
        value = [
            "$UNAUTHORIZED_USER,false", "$AUTHORIZED_USER,true", "$AUTHORIZED_USER,false",
        ]
    )
    fun GET_shouldReturnProprietaryPrivateListAndNonProprietaryPublicList(
        ownerId: String, personal: Boolean
    ) {
        val playlist = testDataProvider.createTestPlaylist(ownerId, personal)
        mvc.get(PLAYLISTS_PATH + "/" + playlist.id)
            .andExpect {
                status { isOk() }
                content { json(jsonTester.write(playlist).json) }
            }
    }

    @Test
    fun GET_shouldReturnForbiddenForNonProprietaryPrivateList() {
        val playlist = testDataProvider.createTestPlaylist(UNAUTHORIZED_USER, true)
        mvc.get(PLAYLISTS_PATH + "/" + playlist.id)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `POST should accept and save valid playlist`() {
        val playlist = Playlist(
            name = "the playlist",
            personal = true,
            songs = testDataProvider.createTestSongs()
        )
        val response = mvc.post(PLAYLISTS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = jsonTester.write(playlist).json
        }.andExpect {
            status { isCreated() }
            header { exists(HttpHeaders.LOCATION) }
        }.andReturn().response

        val id = response.getHeader(HttpHeaders.LOCATION)!!.substringAfterLast('/').toLong()
        val actual = playlistRepository.findByIdOrNull(id)!!
        assertThat(actual.ownerId).isEqualTo(AUTHORIZED_USER)
        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringExpectedNullFields()
            .isEqualTo(playlist)
    }

    @ParameterizedTest
    @FileSource(
        [
            "playlist/invalid/playlistWithId.json",
            "playlist/invalid/playlistWithUserId.json",
            "playlist/invalid/playlistWithoutSongs.json"
        ]
    )
    fun `POST should not accept invalid playlist`(fileContent: String) {
        val amountOfPlaylistsBefore = playlistRepository.count()
        mvc.post(PLAYLISTS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = fileContent
        }.andExpect {
            status { isBadRequest() }
            assertThat(playlistRepository.count()).isEqualTo(amountOfPlaylistsBefore)
        }
    }

    @Test
    internal fun `PUT should update valid playlist`() {
        val playlist = testDataProvider.createTestPlaylist(AUTHORIZED_USER, true)
        val newPlaylist = playlist.copy(
            id = playlist.id,
            name = "new name",
            songs = testDataProvider.createTestSongs()
        )

        mvc.put("$PLAYLISTS_PATH/${playlist.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonTester.write(newPlaylist).json
        }.andExpect {
            status { isNoContent() }
            assertThat(playlistRepository.findByIdOrNull(playlist.id)).isEqualTo(newPlaylist)
        }
    }

    @Test
    internal fun `PUT should not update song when new playlist is empty`() {
        val playlist = testDataProvider.createTestPlaylist(AUTHORIZED_USER, true)
        val newPlaylist = playlist.copy(id = playlist.id, songs = listOf())

        mvc.put("$PLAYLISTS_PATH/${playlist.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonTester.write(newPlaylist).json
        }.andExpect {
            status { isBadRequest() }
            assertThat(playlistRepository.findByIdOrNull(playlist.id)).isEqualTo(playlist)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun DELETE_shouldRemoveProprietaryPlaylistsForAuthorizedUser(personal: Boolean) {
        val playlist = testDataProvider.createTestPlaylist(AUTHORIZED_USER, personal)
        mvc.delete(PLAYLISTS_PATH + "/" + playlist.id)
            .andExpect {
                status { isNoContent() }
                assertThat(playlistRepository.findAll()).doesNotContain(playlist)
            }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun DELETE_shouldReturnForbiddenForNonProprietaryPlaylists(personal: Boolean) {
        val playlist = testDataProvider.createTestPlaylist(UNAUTHORIZED_USER, personal)
        mvc.delete(PLAYLISTS_PATH + "/" + playlist.id)
            .andExpect {
                status { isForbidden() }
                assertThat(playlistRepository.findAll()).contains(playlist)
            }
    }
}
