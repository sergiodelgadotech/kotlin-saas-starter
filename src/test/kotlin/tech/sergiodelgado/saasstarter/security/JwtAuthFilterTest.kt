package tech.sergiodelgado.saasstarter.security

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.micrometer.observation.tck.TestObservationRegistry
import io.micrometer.observation.tck.TestObservationRegistryAssert
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.util.Date

class JwtAuthFilterTest {

    companion object {
        private val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        private const val ISSUER = "https://test.issuer.example.com"
        private const val KID = "test-kid"
        private const val USER_ID = "user-abc-123"
    }

    private val jwk = mockk<Jwk>()
    private val jwkProvider = mockk<JwkProvider>()
    private val filter = JwtAuthFilter(jwkProvider, ISSUER)

    @BeforeEach
    fun setup() {
        every { jwk.publicKey } returns keyPair.public
        every { jwkProvider.get(KID) } returns jwk
    }

    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    private fun buildToken(
        userId: String = USER_ID,
        issuer: String = ISSUER,
        kid: String = KID,
        expiresAt: Date = Date(System.currentTimeMillis() + 3_600_000),
    ): String = JWT.create()
        .withKeyId(kid)
        .withIssuer(issuer)
        .withSubject(userId)
        .withExpiresAt(expiresAt)
        .sign(Algorithm.RSA256(null, keyPair.private as RSAPrivateKey))

    @Test
    fun `valid token sets user id attribute and authentication`() {
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer ${buildToken()}") }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        expectThat(request.getAttribute("auth_user_id")).isEqualTo(USER_ID)
        expectThat(SecurityContextHolder.getContext().authentication).isNotNull()
        expectThat(chain.request).isNotNull()
    }

    @Test
    fun `request without auth header passes through without setting authentication`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        expectThat(request.getAttribute("auth_user_id")).isNull()
        expectThat(SecurityContextHolder.getContext().authentication).isNull()
        expectThat(chain.request).isNotNull()
    }

    @Test
    fun `bearer prefix without token value passes through`() {
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer ") }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        // empty string after stripping prefix: validateAndExtractUserId returns null → 401
        expectThat(response.status).isEqualTo(401)
        expectThat(chain.request).isNull()
    }

    @Test
    fun `expired token returns 401 and does not call chain`() {
        val expired = buildToken(expiresAt = Date(System.currentTimeMillis() - 1_000))
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer $expired") }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        expectThat(response.status).isEqualTo(401)
        expectThat(chain.request).isNull()
    }

    @Test
    fun `token signed by unknown key returns 401`() {
        every { jwkProvider.get(any()) } throws com.auth0.jwk.JwkException("No JWK found for kid")
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer ${buildToken()}") }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        expectThat(response.status).isEqualTo(401)
        expectThat(chain.request).isNull()
    }

    @Test
    fun `token with wrong issuer returns 401`() {
        val wrongIssuer = buildToken(issuer = "https://wrong.issuer.example.com")
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer $wrongIssuer") }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        expectThat(response.status).isEqualTo(401)
        expectThat(chain.request).isNull()
    }

    @Test
    fun `validateAndExtractUserId returns subject from valid token`() {
        val token = buildToken()
        expectThat(filter.validateAndExtractUserId(token)).isEqualTo(USER_ID)
    }

    @Test
    fun `validateAndExtractUserId returns null for expired token`() {
        val token = buildToken(expiresAt = Date(System.currentTimeMillis() - 1_000))
        expectThat(filter.validateAndExtractUserId(token)).isNull()
    }

    @Test
    fun `validateAndExtractUserId returns null for malformed token string`() {
        every { jwkProvider.get(any()) } throws com.auth0.jwk.JwkException("bad")
        expectThat(filter.validateAndExtractUserId("not.a.valid.jwt.at.all")).isNull()
    }

    @Test
    fun `records missing observation when no token is present`() {
        val observationRegistry = TestObservationRegistry.create()
        val observedFilter = JwtAuthFilter(jwkProvider, ISSUER, observationRegistry)
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        observedFilter.doFilter(request, response, MockFilterChain())

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasObservationWithNameEqualTo("saasstarter.auth.jwt")
            .that()
            .hasLowCardinalityKeyValue("outcome", "missing")
    }

    @Test
    fun `records success observation with user id when token is valid`() {
        val observationRegistry = TestObservationRegistry.create()
        val observedFilter = JwtAuthFilter(jwkProvider, ISSUER, observationRegistry)
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer ${buildToken()}") }
        val response = MockHttpServletResponse()

        observedFilter.doFilter(request, response, MockFilterChain())

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasObservationWithNameEqualTo("saasstarter.auth.jwt")
            .that()
            .hasLowCardinalityKeyValue("outcome", "success")
            .hasHighCardinalityKeyValue("user.id", USER_ID)
    }

    @Test
    fun `records invalid observation when token has wrong issuer`() {
        val observationRegistry = TestObservationRegistry.create()
        val observedFilter = JwtAuthFilter(jwkProvider, ISSUER, observationRegistry)
        val wrongIssuer = buildToken(issuer = "https://wrong.issuer.example.com")
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer $wrongIssuer") }
        val response = MockHttpServletResponse()

        observedFilter.doFilter(request, response, MockFilterChain())

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasObservationWithNameEqualTo("saasstarter.auth.jwt")
            .that()
            .hasLowCardinalityKeyValue("outcome", "invalid")
    }

    @Test
    fun `records expired observation when token is expired`() {
        val observationRegistry = TestObservationRegistry.create()
        val observedFilter = JwtAuthFilter(jwkProvider, ISSUER, observationRegistry)
        val expired = buildToken(expiresAt = Date(System.currentTimeMillis() - 1_000))
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer $expired") }
        val response = MockHttpServletResponse()

        observedFilter.doFilter(request, response, MockFilterChain())

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasObservationWithNameEqualTo("saasstarter.auth.jwt")
            .that()
            .hasLowCardinalityKeyValue("outcome", "expired")
    }
}
