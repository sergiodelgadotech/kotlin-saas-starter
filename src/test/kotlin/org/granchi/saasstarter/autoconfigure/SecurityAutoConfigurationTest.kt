package org.granchi.saasstarter.autoconfigure

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.SigningKeyNotFoundException
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.granchi.saasstarter.security.JwtAuthFilter
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import strikt.api.expectThat
import strikt.assertions.hasSize

class SecurityAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration::class.java))

    @Test
    fun `JwkProvider and JwtAuthFilter are registered when jwks-url is set`() {
        contextRunner
            .withPropertyValues(
                "saasstarter.security.jwks-url=https://example.com/oauth/v2/keys",
                "saasstarter.security.issuer=https://example.com",
            )
            .run { context ->
                expectThat(context.getBeansOfType(JwkProvider::class.java)).hasSize(1)
                expectThat(context.getBeansOfType(JwtAuthFilter::class.java)).hasSize(1)
            }
    }

    @Test
    fun `no beans are registered when jwks-url is absent`() {
        contextRunner.run { context ->
            expectThat(context.getBeansOfType(JwkProvider::class.java)).hasSize(0)
            expectThat(context.getBeansOfType(JwtAuthFilter::class.java)).hasSize(0)
        }
    }

    @Test
    fun `no beans are registered when security is disabled`() {
        contextRunner
            .withPropertyValues(
                "saasstarter.security.enabled=false",
                "saasstarter.security.jwks-url=https://example.com/oauth/v2/keys",
            )
            .run { context ->
                expectThat(context.getBeansOfType(JwkProvider::class.java)).hasSize(0)
                expectThat(context.getBeansOfType(JwtAuthFilter::class.java)).hasSize(0)
            }
    }

    @Test
    fun `JwkProvider backs off when consumer provides own bean`() {
        contextRunner
            .withPropertyValues(
                "saasstarter.security.jwks-url=https://example.com/oauth/v2/keys",
                "saasstarter.security.issuer=https://example.com",
            )
            .withBean(JwkProvider::class.java, { mockk<JwkProvider>() })
            .run { context ->
                expectThat(context.getBeansOfType(JwkProvider::class.java)).hasSize(1)
                expectThat(context.getBeansOfType(JwtAuthFilter::class.java)).hasSize(1)
            }
    }

    @Test
    fun `JwtAuthFilter backs off when consumer provides own bean`() {
        contextRunner
            .withPropertyValues(
                "saasstarter.security.jwks-url=https://example.com/oauth/v2/keys",
                "saasstarter.security.issuer=https://example.com",
            )
            .withBean(JwtAuthFilter::class.java, { JwtAuthFilter(mockk<JwkProvider>(), "custom-issuer") })
            .run { context ->
                expectThat(context.getBeansOfType(JwtAuthFilter::class.java)).hasSize(1)
            }
    }

    @Test
    fun `JwtAuthFilter delegates JWT validation to the injected JwkProvider`() {
        val mockProvider = mockk<JwkProvider>()
        every { mockProvider.get(any()) } throws SigningKeyNotFoundException("test", null)

        val fakeToken = JWT.create()
            .withKeyId("test-kid")
            .withIssuer("test-issuer")
            .sign(Algorithm.none())

        val filter = JwtAuthFilter(mockProvider, "test-issuer")
        filter.validateAndExtractUserId(fakeToken)  // returns null; key lookup fails

        verify(exactly = 1) { mockProvider.get("test-kid") }
    }
}
