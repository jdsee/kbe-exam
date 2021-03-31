package htw.ai.kbe.songservice.adapter.security

import htw.ai.kbe.songservice.adapter.security.SecurityConstants.BEARER_TOKEN_PREFIX
import io.jsonwebtoken.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class JwtAuthorizationFilter(
    authenticationManager: AuthenticationManager,
    @Value("\${auth.jwt.key.public}") rsaPublicKey: String
) : BasicAuthenticationFilter(authenticationManager) {
    private val publicKey: PublicKey

    init {
        val key = rsaPublicKey
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
            .replace(" ", "")
        val keySpec = X509EncodedKeySpec(Base64.getDecoder().decode(key))
        publicKey = KeyFactory
            .getInstance(SignatureAlgorithm.RS256.familyName)
            .generatePublic(keySpec)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header == null || header.startsWith(BEARER_TOKEN_PREFIX).not()) {
            filterChain.doFilter(request, response)
            return
        }

        val authentication = getAuthentication(request)
        SecurityContextHolder.getContext().authentication = authentication
        filterChain.doFilter(request, response)
    }

    private fun getAuthentication(request: HttpServletRequest) =
        try {
            val username: String = parseToken(request.getHeader(org.apache.http.HttpHeaders.AUTHORIZATION))
            UsernamePasswordAuthenticationToken(username, null, emptyList())
        } catch (e: ExpiredJwtException) {
            throw AccessDeniedException("Expired token")
        } catch (e: UnsupportedJwtException) {
            throw AccessDeniedException("Unsupported token")
        } catch (e: MalformedJwtException) {
            throw AccessDeniedException("Unsupported token")
        } catch (e: Exception) {
            throw AccessDeniedException("User authorization not resolved")
        } // TODO: extract to global exception handling in gateway

    private fun parseToken(token: String) = Jwts.parserBuilder()
        .setSigningKey(publicKey).build()
        .parseClaimsJws(token.removePrefix(BEARER_TOKEN_PREFIX).trim())
        .body.subject
}
