package htw.ai.kbe.songservice.domain.ports

import htw.ai.kbe.songservice.domain.model.Playlist
import htw.ai.kbe.songservice.domain.model.PlaylistRepository
import htw.ai.kbe.songservice.domain.model.Song
import htw.ai.kbe.songservice.domain.model.SongRepository
import org.springframework.data.domain.Example
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid


/**
 * @author jslg
 */
@Service
class SongService(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val simpleMailSenderClient: SimpleMailSenderClient
) {
    fun getAllSongs(): List<Any> = songRepository.findAll()

    fun getSong(id: Long) = songRepository.findByIdOrNull(id)
        ?: throwNotFound("Song-ID '$id' is not known.")

    fun createSong(@Valid song: Song) = songRepository.save(song)

    fun updateSong(id: Long, song: Song) {
        if (isUnknownSong(id)) throwNotFound("Song-ID '$id' is not known.")
        if (id != song.id) throwBadRequest("Song-ID '$id' from path does not match ID '${song.id}' from body")
        songRepository.save(song)
    }

    fun deleteSong(id: Long) {
        if (isUnknownSong(id)) throwNotFound("Song-ID '$id' is not known.")
        songRepository.deleteById(id);
    }

    fun getAllPlaylists(ownerId: String?, currentUserId: String): List<Playlist> {
        if (ownerId == null) {
            return playlistRepository.findAll()
                .filter { list -> hasAccessPrivilege(currentUserId, list) }
        }
        val allByOwner = playlistRepository.findAllByOwnerId(ownerId);

        return if (currentUserId == ownerId) allByOwner
        else allByOwner.filter { playlist -> playlist.personal.not() }
    }

    fun getPlaylist(id: Long, currentUserId: String): Playlist {
        val playlist = playlistRepository.findByIdOrNull(id)
            ?: throwNotFound("Playlist with '$id' could not be found.")

        return playlist.takeIf { hasAccessPrivilege(currentUserId, it) }
            ?: throwForbidden("Not authorized to read private playlist with id '$id'.")
    }

    fun createPlaylist(playlist: Playlist, currentUserId: String): Playlist {
        if (playlist.id != null || playlist.ownerId != null) {
            throwBadRequest("Id and ownerId of playlist must not be set.")
        }
        playlist.ownerId = currentUserId
        validatePlaylistNotEmpty(playlist)

        return playlistRepository.save(playlist)
    }

    fun updatePlaylist(id: Long, playlist: Playlist, currentUserId: String) {
        if (id != playlist.id) {
            throwBadRequest("The playlist with id '$playlist.id' does not match the id '$id'")
        }
        if (currentUserId != playlist.ownerId) {
            throwBadRequest("The new playlist with ownerId '${playlist.ownerId}' does not match the current user.")
        }
        if (playlist.songs.isEmpty()) {
            throwBadRequest("The new playlist with id '${playlist.id}' must not be empty.")
        }

        val actualPlaylist = getPlaylistIfOwnedBy(id, currentUserId)
        actualPlaylist.name = playlist.name
        actualPlaylist.songs = playlist.songs
        actualPlaylist.personal = playlist.personal

        playlistRepository.save(playlist)
    }

    fun deletePlaylist(id: Long, currentUserId: String) {
        val playlist = getPlaylistIfOwnedBy(id, currentUserId)
        playlistRepository.delete(playlist)
    }

    fun sharePlaylistViaMail(jwtToken: String, currentUserId: String, playlistId: Long, targetEmail: String) {
        val playlist = getPlaylist(playlistId, currentUserId)
        simpleMailSenderClient.sendMail(
            jwtToken,
            SimpleMailRequest(
                targetEmail = targetEmail,
                subject = "Someone want's to share a playlist with you",
                text = playlist.toString()
            )
        )
    }

    private fun getPlaylistIfOwnedBy(
        id: Long,
        currentUserId: String
    ): Playlist {
        val playlist = playlistRepository.findByIdOrNull(id)
            ?: throwNotFound("Playlist-ID is not known.")
        if (currentUserId != playlist.ownerId) {
            throwForbidden("The current user is not authorized to delete playlist with id '$id'")
        }
        return playlist
    }

    private fun validatePlaylistNotEmpty(playlist: Playlist) {
        playlist.songs.stream()
            .filter { song -> songRepository.exists(Example.of(song)) }
            .findAny()
            .orElseThrow { throwBadRequest("Playlist with id '${playlist.id}'must contain at least one song.") }
    }

    private fun isUnknownSong(id: Long) = !songRepository.existsById(id)

    private fun hasAccessPrivilege(userId: String, playlist: Playlist) =
        playlist.personal.not() || userId == playlist.ownerId

    private fun throwForbidden(message: String): Nothing =
        throw ResponseStatusException(HttpStatus.FORBIDDEN, message)

    private fun throwBadRequest(message: String): Nothing =
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, message)

    private fun throwNotFound(message: String): Nothing =
        throw ResponseStatusException(HttpStatus.NOT_FOUND, message)
}
