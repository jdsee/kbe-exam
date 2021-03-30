package htw.ai.kbe.songservice.domain.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.xml.bind.annotation.XmlRootElement

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@Entity
@XmlRootElement
data class Playlist(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var id: Int? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var ownerId: String? = null,
    @ManyToMany(cascade = [CascadeType.MERGE])
    var songs: @NotEmpty MutableList<Song> = mutableListOf(),
    var name: @NotBlank String,
    @JsonProperty("isPrivate")
    var personal: Boolean = false
)
