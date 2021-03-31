package htw.ai.kbe.authservice.domain.model

import org.springframework.data.annotation.Id

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
data class UserCredentials(
    @Id var username: String,
    var password: String
)
