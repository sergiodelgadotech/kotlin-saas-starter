package org.granchi.saasstarter.autoconfigure

import io.mockk.mockk
import org.granchi.saasstarter.ratelimit.RateLimitInterceptor
import org.granchi.saasstarter.ratelimit.RateLimiter
import org.granchi.saasstarter.tenant.TenantInterceptor
import org.granchi.saasstarter.tenant.TenantResolver
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isTrue
import java.util.UUID

class WebMvcAutoConfigurationTest {

    @Suppress("UNCHECKED_CAST")
    private val contextRunner = WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(WebMvcAutoConfiguration::class.java))
        .withBean(
            "redisTemplate",
            RedisTemplate::class.java as Class<RedisTemplate<String, Any>>,
            { mockk<RedisTemplate<String, Any>>(relaxed = true) }
        )
        .withBean(TenantResolver::class.java, { TenantResolver { _ -> UUID.randomUUID() } })

    @Test
    fun `RateLimiter, TenantInterceptor and RateLimitInterceptor are registered`() {
        contextRunner.run { context ->
            expectThat(context.getBeansOfType(RateLimiter::class.java)).hasSize(1)
            expectThat(context.getBeansOfType(TenantInterceptor::class.java)).hasSize(1)
            expectThat(context.getBeansOfType(RateLimitInterceptor::class.java)).hasSize(1)
        }
    }

    @Test
    fun `WebMvcAutoConfiguration is itself a WebMvcConfigurer bean`() {
        contextRunner.run { context ->
            val configurers = context.getBeansOfType(WebMvcConfigurer::class.java)
            expectThat(configurers.values.any { it is WebMvcAutoConfiguration }).isTrue()
        }
    }

    @Test
    fun `tenant interceptor is skipped when tenant enabled is false`() {
        contextRunner
            .withPropertyValues("saasstarter.tenant.enabled=false")
            .run { context ->
                expectThat(context.getBeansOfType(TenantInterceptor::class.java)).hasSize(0)
            }
    }

    @Test
    fun `rate limit interceptor is skipped when rate-limit enabled is false`() {
        contextRunner
            .withPropertyValues("saasstarter.rate-limit.enabled=false")
            .run { context ->
                expectThat(context.getBeansOfType(RateLimitInterceptor::class.java)).hasSize(0)
            }
    }
}
