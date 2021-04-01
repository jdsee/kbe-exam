package htw.ai.kbe.songservice.adapter.api

import htw.ai.kbe.songservice.domain.ports.SongService
import org.springframework.web.bind.annotation.*

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@RestController
@RequestMapping("/playlists/share")
class SharePlaylistController(
    private val songService: SongService
) {
    @GetMapping(value = ["/{id}"])
    fun sharePlaylist(
        @PathVariable("id") playlistId: Long
    ) = songService.sharePlaylistViaMail(playlistId)
}
