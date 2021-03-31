package htw.ai.kbe.authservice.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import htw.ai.kbe.authservice.domain.model.UserCredentials
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class JwtAuthenticationFilter(
    private val authManager: AuthenticationManager
) : UsernamePasswordAuthenticationFilter() {
    private val objectMapper = jacksonObjectMapper()

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {
        if (!HttpMethod.POST.matches(request.method) || MediaType.APPLICATION_JSON_VALUE != request.contentType) {
            throw AuthenticationServiceException(
                "Authentication requires POST method instead of: " + request.method
            )
        }
        return try {
            val credentials: UserCredentials = objectMapper.readValue(request.inputStream)
            val authToken = UsernamePasswordAuthenticationToken(credentials.username, credentials.password)
            setDetails(request, authToken)
            authManager.authenticate(authToken)
        } catch (e: IOException) {
            throw AuthenticationCredentialsNotFoundException(
                "Authentication credentials could not been resolved", e
            )
        }
    }
}
