package org.granchi.saasstarter.autoconfigure

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import org.granchi.saasstarter.security.JwtAuthFilter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.TimeUnit

/**
 * Wires a cached, rate-limited [JwkProvider] and a [JwtAuthFilter] that validates
 * RS256 JWTs using keys fetched from the configured JWKS endpoint.
 *
 * Requires [saasstarter.security.jwks-url] to be set; backs off if disabled via
 * [saasstarter.security.enabled=false] or when the consumer supplies its own beans.
 */
@AutoConfiguration
@ConditionalOnClass(JWT::class, OncePerRequestFilter::class)
@ConditionalOnProperty(
    prefix = "saasstarter.security",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@EnableConfigurationProperties(SaasStarterProperties::class)
class SecurityAutoConfiguration(private val properties: SaasStarterProperties) {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnExpression("!'\${saasstarter.security.jwks-url:}'.isEmpty()")
    fun jwkProvider(): JwkProvider =
        JwkProviderBuilder(properties.security.jwksUrl)
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JwkProvider::class)
    fun jwtAuthFilter(jwkProvider: JwkProvider): JwtAuthFilter =
        JwtAuthFilter(jwkProvider, properties.security.issuer)
}
