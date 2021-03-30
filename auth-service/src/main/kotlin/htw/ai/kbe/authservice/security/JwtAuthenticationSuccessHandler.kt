package htw.ai.kbe.authservice.security

import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/

const val BEARER = "Bearer "

class JwtAuthenticationSuccessHandler(
    private val jwtService: JwtService
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        response.contentType = MediaType.TEXT_PLAIN_VALUE
        response.writer.println(
            BEARER + jwtService.generateToken((authentication.principal as User).username)
        )
    }
}
