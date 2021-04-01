package htw.ai.kbe.songservice.adapter.api

import htw.ai.kbe.songservice.domain.ports.SongService
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import java.security.Principal
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.PositiveOrZero

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@RestController
@RequestMapping("/share-playlist")
class SharePlaylistController(
    private val songService: SongService
) {
    @PostMapping
    fun sharePlaylist(
        @Valid @RequestBody request: SharePlaylistRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) jwtToken: String,
        principal: Principal
    ) = songService.sharePlaylistViaMail(jwtToken, principal.name, request.playlistId, request.targetEmail)
}

data class SharePlaylistRequest(
    @PositiveOrZero
    val playlistId: Long,
    @Email
    val targetEmail: String
)
