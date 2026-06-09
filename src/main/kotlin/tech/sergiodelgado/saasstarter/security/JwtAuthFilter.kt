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
 * Validates RS256 JWTs from any OIDC provider (Zitadel in the reference template).
 *
 * Reads the `sub` claim and sets it as the `auth_user_id` request attribute.
 * The app's [tech.sergiodelgado.saasstarter.tenant.TenantResolver] maps that ID
 * to the `external_user_id` column in the `members` table.
 *
 * JWKS endpoint and issuer are supplied via `saasstarter.security.jwks-url` and
 * `saasstarter.security.issuer`.
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
        var userId: String? = null
        val outcome: String = if (token == null) {
            "missing"
        } else {
            try {
                userId = verifyAndExtractUserId(token)
                "success"
            } catch (e: TokenExpiredException) {
                "expired"
            } catch (e: Exception) {
                logger.debug("JWT validation failed: ${e.message}")
                "invalid"
            }
        }

        val obs = Observation.createNotStarted("saasstarter.auth.jwt", observationRegistry)
            .lowCardinalityKeyValue("outcome", outcome)
            .start()
        try {
            if (userId != null) obs.highCardinalityKeyValue("user.id", userId)
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
        } finally {
            obs.stop()
        }
    }

    private fun extractToken(request: HttpServletRequest): String? =
        request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)

    private fun verifyAndExtractUserId(token: String): String {
        val jwt = JWT.decode(token)
        val jwk = jwkProvider.get(jwt.keyId)
        val algorithm = Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null)
        val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()
        return verifier.verify(token).subject
    }

    internal fun validateAndExtractUserId(token: String): String? = try {
        verifyAndExtractUserId(token)
    } catch (e: Exception) {
        logger.debug("JWT validation failed: ${e.message}")
        null
    }

    companion object {
        private const val USER_ID_ATTR = "auth_user_id"

        fun getUserId(request: HttpServletRequest): String? =
            request.getAttribute(USER_ID_ATTR) as? String
    }
}
