package tech.sergiodelgado.saasstarter.security

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.security.interfaces.RSAPublicKey

/**
 * Validates JWTs issued by Zitadel.
 *
 * Zitadel issues standard OIDC JWTs signed with RS256.
 * The JWKS endpoint is at: https://<your-zitadel-domain>/oauth/v2/keys
 *
 * The `sub` claim contains the Zitadel user ID, which maps to
 * the `zitadel_user_id` column in the `members` table.
 */
class JwtAuthFilter(
    private val jwkProvider: JwkProvider,
    private val issuer: String,
    private val observationRegistry: ObservationRegistry = ObservationRegistry.NOOP,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val token = extractToken(request)
        val userId = token?.let { validateAndExtractUserId(it) }

        val outcome = when {
            token == null -> "missing"
            userId != null -> "success"
            else -> outcomeFor(token)
        }

        val obs = Observation.createNotStarted("saasstarter.auth.jwt", observationRegistry)
            .lowCardinalityKeyValue("outcome", outcome)
        if (userId != null) obs.highCardinalityKeyValue("user.id", userId)
        obs.observe { }

        when {
            token == null -> {
                chain.doFilter(request, response)
            }
            userId == null -> {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            }
            else -> {
                request.setAttribute(USER_ID_ATTR, userId)
                val auth = UsernamePasswordAuthenticationToken(userId, null, emptyList())
                SecurityContextHolder.getContext().authentication = auth
                chain.doFilter(request, response)
            }
        }
    }

    private fun extractToken(request: HttpServletRequest): String? =
        request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)

    internal fun validateAndExtractUserId(token: String): String? = try {
        val jwt = JWT.decode(token)
        val jwk = jwkProvider.get(jwt.keyId)
        val algorithm = Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null)
        val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()
        verifier.verify(token).subject  // sub = Zitadel user ID
    } catch (e: Exception) {
        logger.debug("JWT validation failed: ${e.message}")
        null
    }

    private fun outcomeFor(token: String): String = try {
        val jwt = JWT.decode(token)
        val jwk = jwkProvider.get(jwt.keyId)
        val algorithm = Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null)
        JWT.require(algorithm).withIssuer(issuer).build().verify(token)
        "invalid"  // verification succeeded but userId was null — should not happen; treat as invalid
    } catch (e: TokenExpiredException) {
        "expired"
    } catch (e: Exception) {
        "invalid"
    }

    companion object {
        private const val USER_ID_ATTR = "auth_user_id"

        fun getUserId(request: HttpServletRequest): String? =
            request.getAttribute(USER_ID_ATTR) as? String
    }
}
