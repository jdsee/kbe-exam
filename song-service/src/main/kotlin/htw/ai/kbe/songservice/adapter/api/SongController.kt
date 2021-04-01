package htw.ai.kbe.songservice.adapter.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import htw.ai.kbe.songservice.adapter.api.ApiEndpointConstants.SONGS_PATH
import htw.ai.kbe.songservice.domain.model.Song
import htw.ai.kbe.songservice.domain.ports.SongService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.validation.Valid
import javax.validation.Validation
import javax.validation.constraints.NotBlank


@RestController
@RequestMapping(SONGS_PATH)
class SongController(
    private val songService: SongService
) {
    private val objectMapper = jacksonObjectMapper()
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @GetMapping(
        produces = [
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_XML_VALUE]
    )
    fun getAllSongs() = songService.getAllSongs()

    @GetMapping(
        path = ["/{id}"],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun getSong(@PathVariable id: Long) = songService.getSong(id)

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createSong(
        @RequestBody @Valid song: SongRequest
    ): ResponseEntity<*> {
        val id = songService.createSong(song.toEntity()).id
        return buildCreatedResponse(id)
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createSongByFile(
        @RequestParam("file") jsonSong: MultipartFile
    ): ResponseEntity<*> {
        val song: SongRequest = objectMapper.readValue(jsonSong.bytes)
        if (validator.validate(song).isNotEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The song from the given json-file is not valid.")
        }
        return createSong(song)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteSong(
        @PathVariable("id") id: Long
    ) = songService.deleteSong(id)

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateSong(
        @PathVariable("id") id: Long,
        @RequestBody @Valid song: Song
    ) = songService.updateSong(id, song)

    private fun buildCreatedResponse(id: Long?): ResponseEntity<Void> =
        ResponseEntity.created(
            ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id).toUri()
        ).build()
}

data class SongRequest(
    @get:NotBlank(message = "Title must not be empty.")
    var title: String,
    var artist: String? = null,
    var album: String? = null,
    var released: Int? = null
) {
    fun toEntity() = Song(title, artist, album, released)
}
