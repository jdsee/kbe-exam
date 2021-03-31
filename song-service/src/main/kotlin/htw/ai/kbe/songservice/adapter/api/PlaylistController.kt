package htw.ai.kbe.songservice.adapter.api

import htw.ai.kbe.songservice.adapter.api.ApiEndpointConstants.PLAYLISTS_PATH
import htw.ai.kbe.songservice.domain.model.Playlist
import htw.ai.kbe.songservice.domain.ports.SongService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.security.Principal
import javax.validation.Valid
import javax.validation.constraints.Positive

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@RestController
@RequestMapping(PLAYLISTS_PATH)
class PlaylistController
@Autowired constructor(
    private val songService: SongService
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun getAllSongLists(
        @RequestParam(name = "ownerId") ownerId: String?,
        principal: Principal
    ): List<Playlist> = songService.getAllPlaylists(ownerId, principal.name)

    @GetMapping(
        value = ["/{id}"],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun getSongList(
        @PathVariable id: @Positive Long,
        principal: Principal
    ): Playlist = songService.getSongList(id, principal.name)

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createSong(
        @RequestBody playlist: @Valid Playlist,
        principal: Principal,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<*> {
        val result: Playlist = songService.createPlaylist(playlist, principal.name)
        val location = uriComponentsBuilder.path("/{id}")
            .buildAndExpand(result.id).toUri()
        return ResponseEntity.created(location).build<Any>()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteSongList(
        @PathVariable id: @Positive Long,
        principal: Principal
    ) = songService.deletePlaylist(id, principal.name)
}
