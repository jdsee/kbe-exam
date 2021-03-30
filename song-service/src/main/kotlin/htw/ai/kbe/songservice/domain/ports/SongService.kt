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
    private val playlistRepository: PlaylistRepository
) {
    fun getAllSongs(): List<Any> = songRepository.findAll()

    fun getSong(id: Long) = songRepository.findByIdOrNull(id)
        ?: throwNotFound("Song-ID '$id' is not known.")

    fun createSong(@Valid song: Song) = songRepository.save(song)

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

    fun getSongList(id: Long, currentUserId: String): Playlist {
        val playlist = playlistRepository.findByIdOrNull(id)
            ?: throwNotFound("Playlist-ID '$id' is not known.")

        return playlist.takeIf { p -> hasAccessPrivilege(currentUserId, p) }
            ?: throwUnauthorized("Not authorized to read private playlist with id '$id'.")
    }

    fun createPlaylist(playlist: Playlist, currentUserId: String): Playlist {
        if (playlist.id != null || playlist.ownerId != null) {
            throwBadRequest("Id and ownerId of playlist must not be set.")
        }
        playlist.ownerId = currentUserId
        playlist.songs.stream()
            .filter { song -> songRepository.exists(Example.of(song)) }.findAny()
            ?: throwBadRequest("Playlist must contain at least one song.")

        return playlistRepository.save(playlist)
    }

    fun deletePlaylist(id: Long, currentUserId: String) {
        val playlist = playlistRepository.findByIdOrNull(id)
            ?: throwNotFound("Playlist-ID is not known.")
        if (currentUserId == playlist.ownerId) {
            throwUnauthorized("The current user is not authorized to delete playlist with id '$id'")
        }
        playlistRepository.delete(playlist)
    }

    private fun isUnknownSong(id: Long) = !songRepository.existsById(id)

    private fun hasAccessPrivilege(userId: String, playlist: Playlist) =
        playlist.personal.not() || userId == playlist.ownerId

    private fun throwUnauthorized(message: String): Nothing =
        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, message)

    private fun throwBadRequest(message: String): Nothing =
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, message)

    private fun throwNotFound(message: String): Nothing =
        throw ResponseStatusException(HttpStatus.NOT_FOUND, message)
}
