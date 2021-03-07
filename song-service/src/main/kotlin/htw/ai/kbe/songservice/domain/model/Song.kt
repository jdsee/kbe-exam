package htw.ai.kbe.songservice.domain.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@Entity
@XmlRootElement
data class Song(
        @XmlElement
        @JsonInclude(Include.NON_EMPTY)
        @field:NotBlank(message = "Title must not be empty.")
        var title: String,
        @XmlElement
        var artist: String? = null,
        @XmlElement
        var album: String? = null,
        @XmlElement
        var released: Int? = null,
        @XmlElement
        @Id
        @GeneratedValue
        @JsonInclude(Include.NON_NULL)
        var id: Long? = null
)
