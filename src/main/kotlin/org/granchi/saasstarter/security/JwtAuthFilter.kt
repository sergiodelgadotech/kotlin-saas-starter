package org.granchi.saasstarter.security

import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.net.URI
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
@Component
class JwtAuthFilter : OncePerRequestFilter() {

    @Value("\${auth.jwks-url}")
    private lateinit var jwksUrl: String

    @Value("\${auth.issuer}")
    private lateinit var issuer: String

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val token = extractToken(request) ?: run {
            chain.doFilter(request, response)
            return
        }

        val userId = validateAndExtractUserId(token) ?: run {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }

        request.setAttribute(USER_ID_ATTR, userId)

        val auth = UsernamePasswordAuthenticationToken(userId, null, emptyList())
        SecurityContextHolder.getContext().authentication = auth

        chain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? =
        request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)

    private fun validateAndExtractUserId(token: String): String? = try {
        val jwkProvider = UrlJwkProvider(URI.create(jwksUrl).toURL())
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

    companion object {
        private const val USER_ID_ATTR = "auth_user_id"

        fun getUserId(request: HttpServletRequest): String? =
            request.getAttribute(USER_ID_ATTR) as? String
    }
}
