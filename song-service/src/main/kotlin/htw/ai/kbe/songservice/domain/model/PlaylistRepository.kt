package htw.ai.kbe.songservice.domain.model

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@Repository
interface PlaylistRepository : JpaRepository<Playlist, Long> {
    fun findAllByOwnerId(ownerId: String): List<Playlist>
}
