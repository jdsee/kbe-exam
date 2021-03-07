package htw.ai.kbe.songservice.adapter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import htw.ai.kbe.songservice.ApiEndpointConstants
import htw.ai.kbe.songservice.ApiEndpointConstants.SONGS_PATH
import htw.ai.kbe.songservice.domain.model.Song
import htw.ai.kbe.songservice.domain.model.SongRepository
import htw.ai.kbe.songservice.testutils.parameterized.FileSource
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.matchesRegex
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional


private const val SONG_JSON_FILE_NAME = "song.json"
private const val SONG_MULTIPART_FILE_NAME = "file"

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
internal class SongControllerTest
@Autowired constructor(
        private val mvc: MockMvc,
        val songRepository: SongRepository,
) {
    private val objectMapper = jacksonObjectMapper()

    @ParameterizedTest
    @CsvSource(value = ["application/json,json", "application/xml,xml", "*/*,json"])
    internal fun `GET should support json and xml responses`(
            acceptHeader: String, expectedContentType: String
    ) {
        mvc.get(SONGS_PATH) {
            accept = MediaType.valueOf(acceptHeader)
        }.andExpect {
            status { isOk() }
            content {
                contentType("application/$expectedContentType")
                string(matchesRegex(if (expectedContentType == "json") "\\[.*" else "<.*"))
            }
        }
    }

    @Test
    internal fun `GET should return song when Song-ID is known`() {
        val song: Song = createTestSong()
        mvc.get("$SONGS_PATH/${song.id}") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                content { objectMapper.writeValueAsString(song) }
            }
        }
    }

    @Test
    internal fun `GET should return BAD REQUEST when Song-ID is not known`() {
        mvc.get("$SONGS_PATH/3000") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() } // TODO: check if 400 is more appropriate
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "{\"title\":\"title\",\"artist\":null,\"album\":null,\"released\":0}",
        "{\"title\":\"title\",\"artist\":null,\"album\":\"album\",\"released\":0}",
        "{\"title\":\"title\",\"artist\":null,\"album\":null,\"released\":100}",
        "{\"title\":\"title\",\"artist\":\"jolu\",\"album\":null,\"released\":0}",
        "{\"title\":\"title\",\"artist\":\"jolu\",\"album\":\"album\",\"released\":0}"
    ])
    fun `POST should create valid song and set proper location header`(requestBody: String) {
        mvc.post(SONGS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isCreated() }
            header {
                string(HttpHeaders.LOCATION, matchesRegex(".*$SONGS_PATH/${findMaxId()}"))
            }
        }
    }

    @ParameterizedTest
    @FileSource([
        "songs/invalid/songWithBlankTitle.json",
        "songs/invalid/songWithNoTitle.json",
        "songs/invalid/songWithNullTitle.json",
        "songs/invalid/songWithId.json"
    ])
    fun `POST should not create invalid songs`(requestBody: String) {
        mvc.post(ApiEndpointConstants.SONGS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @ParameterizedTest
    @FileSource(["songs/valid/song.json"])
    fun `POST should accept and save valid song from JSON-file`(fileContent: String) {
        mvc.multipart(SONGS_PATH) {
            contentType = MediaType.MULTIPART_FORM_DATA
            file(createSongMockMultipartFile(fileContent))
        }.andExpect {
            status { isCreated() }
            assertThat(songRepository.findAll()[0])
                    .usingRecursiveComparison()
                    .ignoringExpectedNullFields()
                    .isEqualTo(Song(
                            title = "We Built This City",
                            artist = "Starship",
                            album = "Grunt/RCA",
                            released = 1985
                    ))
        }
    }

    @ParameterizedTest
    @FileSource([
        "songs/invalid/songWithBlankTitle.json",
        "songs/invalid/songWithNoTitle.json",
        "songs/invalid/songWithNullTitle.json",
        "songs/invalid/songWithId.json"
    ])
    fun `POST should not accept and save invalid song from JSON-File`(fileContent: String) {
        mvc.multipart(SONGS_PATH) {
            file(createSongMockMultipartFile(fileContent))
        }.andExpect {
            status { isBadRequest() }
            assertThat(songRepository.findAll()).isEmpty()
        }
    }

    private fun createSongMockMultipartFile(fileContent: String) = MockMultipartFile(
            SONG_MULTIPART_FILE_NAME,
            SONG_JSON_FILE_NAME,
            MediaType.APPLICATION_JSON_VALUE,
            fileContent.toByteArray())

    private fun findMaxId() =
            songRepository.findAll().maxByOrNull { it.id!! }?.id
                    ?: throw AssertionError("No instance of song could be found in database.")

    private fun createTestSong() =
            songRepository.save(Song(
                    title = "My Friend the Forest",
                    artist = "Nils Frahm",
                    album = "All Melody",
                    released = 2018
            ))
}
