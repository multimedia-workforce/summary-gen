package jku.multimediasysteme.shared.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.spec.SecretKeySpec

/**
 * Service for generating and validating JWTs.
 * Uses HMAC with SHA-256 (HS256) for signature.
 */
@Service
class JwtService(
    @Value("\${jwt.secret}") private val secret: String, // Secret key from application.yaml
    @Value("\${jwt.expiration}") private val expiration: Long // Expiration time in milliseconds
) {
    // Create a signing key using the provided secret and HS256 algorithm
    private val key = SecretKeySpec(Base64.getDecoder().decode(secret), SignatureAlgorithm.HS256.jcaName)

    /**
     * Generates a signed JWT for the given user ID.
     *
     * @param userId The UUID of the user for whom the token is created.
     * @return A serialized JWT string.
     */
    fun generateToken(userId: UUID): String {
        val now = Date()

        return Jwts.builder()
            .setSubject(userId.toString())                        // Store userId as subject
            .setIssuedAt(now)                                     // Set issue time to current time
            .setExpiration(Date(now.time + expiration))           // Set expiration time
            .signWith(key)                                        // Sign the token with secret key
            .compact()                                            // Build and serialize the token
    }

    /**
     * Extracts the user ID from the token's subject claim.
     *
     * @param token A JWT string.
     * @return The user ID (subject claim) as a string.
     * @throws io.jsonwebtoken.JwtException if the token is invalid or malformed.
     */
    fun extractUserId(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(key)                                  // Provide the signing key to validate JWT signature
            .build()
            .parseClaimsJws(token)                               // Parse & validate JWT (throws if invalid/expired)
            .body                                                // Access the payload (claims)
            .subject                                             // Return the 'sub' claim (user ID)
    }
}