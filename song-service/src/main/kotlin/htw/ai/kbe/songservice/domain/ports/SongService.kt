package htw.ai.kbe.songservice.domain.ports

import htw.ai.kbe.songservice.domain.model.Song
import htw.ai.kbe.songservice.domain.model.SongRepository
import org.springframework.stereotype.Service
import javax.validation.Valid

/**
 * @author jslg
 */
@Service
class SongService(
        private val songRepository: SongRepository,
) {
    fun getAllSongs(): List<Any> = songRepository.findAll()
    fun getSong(id: Long) = songRepository.getOne(id)
    fun createSong(@Valid song: Song) = songRepository.save(song)
}
