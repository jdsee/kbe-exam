package htw.ai.kbe.songservice

import htw.ai.kbe.songservice.domain.model.Playlist
import htw.ai.kbe.songservice.domain.model.PlaylistRepository
import htw.ai.kbe.songservice.domain.model.Song
import htw.ai.kbe.songservice.domain.model.SongRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
class SongServiceApplication {
    @Bean
    fun demoData(songRepo: SongRepository, playlistRepo: PlaylistRepository) =
        CommandLineRunner {
            val song1 = songRepo.save(
                Song(
                    title = "My Friend the Forest",
                    artist = "Nils Frahm",
                    album = "All Melody",
                    released = 2018
                )
            )
            val song2 = songRepo.save(
                Song(
                    title = "Music is cool",
                    artist = "Musician#1",
                    album = "Cool album",
                    released = 2021
                )
            )
            val song3 = songRepo.save(
                Song(
                    title = "Taken Effect",
                    artist = "Shed",
                    album = "The Final Experiment",
                    released = 1995
                )
            )
            playlistRepo.saveAll(
                listOf(
                    Playlist(
                        name = "private playlist user 1",
                        ownerId = "user1",
                        songs = listOf(song1, song3),
                        personal = true
                    ),
                    Playlist(
                        name = "public playlist user 1",
                        ownerId = "user1",
                        songs = listOf(song2),
                        personal = false
                    ),
                    Playlist(
                        name = "public playlist user 2",
                        ownerId = "user2",
                        songs = listOf(song1, song2, song3),
                        personal = false
                    )
                )
            )
        }
}

fun main(args: Array<String>) {
    runApplication<SongServiceApplication>(*args)
}
