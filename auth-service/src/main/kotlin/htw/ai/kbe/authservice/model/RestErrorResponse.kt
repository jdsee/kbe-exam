package htw.ai.kbe.authservice.model

import org.springframework.http.HttpStatus

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
data class RestErrorResponse(
    val statusCode: Int,
    val reason: String,
    val detailMessage: String
) {
    companion object {
        fun of(status: HttpStatus, detailMessage: String): RestErrorResponse =
            RestErrorResponse(status.value(), status.reasonPhrase, detailMessage)
    }
}
