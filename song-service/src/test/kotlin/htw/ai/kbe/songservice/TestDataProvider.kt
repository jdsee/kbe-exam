package htw.ai.kbe.songservice

import htw.ai.kbe.songservice.domain.model.Playlist
import htw.ai.kbe.songservice.domain.model.PlaylistRepository
import htw.ai.kbe.songservice.domain.model.Song
import htw.ai.kbe.songservice.domain.model.SongRepository
import org.springframework.stereotype.Component

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@Component
internal class TestDataProvider(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository
) {
    internal fun createTestSong() =
        songRepository.save(
            Song(
                title = "My Friend the Forest",
                artist = "Nils Frahm",
                album = "All Melody",
                released = 2018
            )
        )

    internal fun createTestSongs() =
        songRepository.saveAll(
            listOf(
                Song(
                    title = "best song ever",
                    artist = "best artist ever",
                    album = "best album ever",
                    released = 2042
                ),
                Song(
                    title = "second best song ever",
                    artist = "best artist ever",
                    album = "best album ever",
                    released = 2042
                )
            )
        )

    internal fun createTestPlaylist(ownerId: String, personal: Boolean) =
        playlistRepository.save(
            Playlist(
                name = "the playlist",
                personal = personal,
                ownerId = ownerId,
                songs = mutableListOf(
                    songRepository.save(
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
        )
}
