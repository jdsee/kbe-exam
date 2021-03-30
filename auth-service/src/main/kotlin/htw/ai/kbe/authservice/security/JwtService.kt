package htw.ai.kbe.authservice.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class JwtService(
    @Value("#auth.jwt.expiry:15") tokenExpiryDuration: Long,
    @Value("#auth.jwt.key.private") rsaPrivateKey: String
) {
    private val tokenExpiryDuration = Duration.ofMinutes(tokenExpiryDuration)
    private val privateKey: PrivateKey

    init {
        rsaPrivateKey.removePrefix("-----BEGIN RSA PRIVATE KEY-----")
        rsaPrivateKey.removeSuffix("-----END RSA PRIVATE KEY-----")
        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaPrivateKey))
        privateKey = KeyFactory
            .getInstance(SignatureAlgorithm.RS256.value)
            .generatePrivate(keySpec)
    }

    fun generateToken(subject: String): String {
        val now = Instant.now()
        return Jwts.builder()
            .setSubject(subject)
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(tokenExpiryDuration)))
            .signWith(privateKey)
            .compact()
    }
}
