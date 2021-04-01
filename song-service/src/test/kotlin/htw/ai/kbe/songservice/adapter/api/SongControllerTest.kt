package htw.ai.kbe.songservice.adapter.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import htw.ai.kbe.songservice.TestDataProvider
import htw.ai.kbe.songservice.adapter.api.ApiEndpointConstants.SONGS_PATH
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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.*
import org.springframework.transaction.annotation.Transactional


private const val SONG_JSON_FILE_NAME = "song.json"
private const val SONG_MULTIPART_FILE_NAME = "file"

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@WithMockUser
internal class SongControllerTest
@Autowired constructor(
    private val mvc: MockMvc,
    private val songRepository: SongRepository,
    private val testDataProvider: TestDataProvider
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
        val song: Song = testDataProvider.createTestSong()
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
    @ValueSource(
        strings = [
            """{"title":"title","artist":null,"album":null,"released":0}""",
            """{"title":"title","artist":null,"album":"album","released":0}""",
            """{"title":"title","artist":null,"album":null,"released":100}""",
            """{"title":"title","artist":"jolu","album":null,"released":0}""",
            """{"title":"title","artist":"jolu","album":"album","released":0}"""
        ]
    )
    internal fun `POST should create valid song and set proper location header`(requestBody: String) {
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
    @FileSource(
        [
            "songs/invalid/songWithBlankTitle.json",
            "songs/invalid/songWithNoTitle.json",
            "songs/invalid/songWithNullTitle.json",
            "songs/invalid/songWithId.json"
        ]
    )
    internal fun `POST should not create invalid songs`(requestBody: String) {
        mvc.post(SONGS_PATH) {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @ParameterizedTest
    @FileSource(["songs/valid/song.json"])
    internal fun `POST should accept and save valid song from JSON-file`(fileContent: String) {
        val response = mvc.multipart(SONGS_PATH) {
            contentType = MediaType.MULTIPART_FORM_DATA
            file(createSongMockMultipartFile(fileContent))
        }.andExpect {
            status { isCreated() }
            header { exists(HttpHeaders.LOCATION) }
        }.andReturn().response

        val id = response.getHeader(HttpHeaders.LOCATION)!!.substringAfterLast('/').toLong()
        assertThat(songRepository.findByIdOrNull(id))
            .isEqualTo(
                Song(
                    id = id,
                    title = "We Built This City",
                    artist = "Starship",
                    album = "Grunt/RCA",
                    released = 1985
                )
            )
    }

    @ParameterizedTest
    @FileSource(
        [
            "songs/invalid/songWithBlankTitle.json",
            "songs/invalid/songWithNoTitle.json",
            "songs/invalid/songWithNullTitle.json",
            "songs/invalid/songWithId.json"
        ]
    )
    internal fun `POST should not accept and save invalid song from JSON-File`(fileContent: String) {
        val amountOfSongsBefore = songRepository.count()
        mvc.multipart(SONGS_PATH) {
            file(createSongMockMultipartFile(fileContent))
        }.andExpect {
            status { isBadRequest() }
            assertThat(songRepository.count()).isEqualTo(amountOfSongsBefore)
        }
    }

    @Test
    fun `PUT should not update song with invalid ID`() {
        val id = testDataProvider.createTestSong().id
        mvc.put("$SONGS_PATH/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createTestSong())
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            """{"id":@id,"title":"title","artist":"jolu","album":"album","released":2020}""",
            """{"id":@id,"title":"title"}"""]
    )
    fun `PUT should update valid song`(reqBody: String) {
        val id = testDataProvider.createTestSong().id
        mvc.put("$SONGS_PATH/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = reqBody.replace("@id", id.toString())
        }.andExpect {
            status { isNoContent() }
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            """{"title":"XXX","artist":"jolu","label":"label","released":2020}#100#true""", // no id
            """{"id":100,"title":"XXX","artist":"jolu","label":"label","released":2020}#111#true""", // id's not matching
            """{"id":100,"artist":"jolu","label":"label","released":2020}#100#true""", // no title
            """{"id":100,"title":"title","artist":"jolu","label":"label","released":2020}#100#false"""], // song not present
        delimiter = '#'
    )
    fun `PUT should not update invalid song`(
        reqBody: String?,
        pathId: Int,
        songPresent: Boolean
    ) {
        if (songPresent) {
            songRepository.save(createTestSong())
        }
        mvc.put("$SONGS_PATH/$pathId") {
            contentType = MediaType.APPLICATION_JSON
            content = reqBody
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `DELETE should return NO CONTENT and delete when id is known`() {
        val amountOfSongsBefore = songRepository.count()
        val id = testDataProvider.createTestSong().id
        mvc.delete("$SONGS_PATH/$id")
            .andExpect {
                status { isNoContent() }
                assertThat(songRepository.count()).isEqualTo(amountOfSongsBefore)
            }
    }

    @Test
    fun `DELETE should return NOT FOUND when id is no known`() {
        mvc.delete("$SONGS_PATH/123")
            .andExpect {
                status { isNotFound() }
            }
    }

    private fun createSongMockMultipartFile(fileContent: String) = MockMultipartFile(
        SONG_MULTIPART_FILE_NAME,
        SONG_JSON_FILE_NAME,
        MediaType.APPLICATION_JSON_VALUE,
        fileContent.toByteArray()
    )

    private fun findMaxId() =
        songRepository.findAll().maxByOrNull { it.id!! }?.id
            ?: throw AssertionError("No instance of song could be found in database.")

    private fun createTestSong() =
        songRepository.save(
            Song(
                title = "My Friend the Forest",
                artist = "Nils Frahm",
                album = "All Melody",
                released = 2018
            )
        )
}
