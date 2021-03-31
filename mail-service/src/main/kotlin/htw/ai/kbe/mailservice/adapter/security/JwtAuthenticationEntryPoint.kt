package htw.ai.kbe.mailservice.adapter.security

import htw.ai.kbe.mailservice.adapter.security.SecurityConstants.BEARER_TOKEN_PREFIX
import htw.ai.kbe.mailservice.adapter.security.SecurityConstants.UNAUTHORIZED_REALM
import org.springframework.http.HttpHeaders
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "$BEARER_TOKEN_PREFIX $UNAUTHORIZED_REALM")
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.message)
    }
}
