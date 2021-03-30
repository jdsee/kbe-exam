package htw.ai.kbe.authservice.model

import org.springframework.data.annotation.Id

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
data class UserCredentials(
    @Id var id: String?,
    var username: String,
    var password: String
)
