package htw.ai.kbe.authservice.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import htw.ai.kbe.authservice.model.RestErrorResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import java.io.Writer
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.MediaType

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
class JwtAuthenticationFailureHandler : AuthenticationFailureHandler {

    private val objectMapper = jacksonObjectMapper()

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON
        response.addHeader(HttpHeaders.WWW_AUTHENTICATE, """Bearer realm="song-api-realm"""")
        writeResponse(response.writer, exception)
    }

    private fun writeResponse(writer: Writer, exception: AuthenticationException) {
        objectMapper.writeValue(
            writer,
            RestErrorResponse.of(HttpStatus.UNAUTHORIZED, exception.message ?: "Authentication failed.")
        )
    }
}
