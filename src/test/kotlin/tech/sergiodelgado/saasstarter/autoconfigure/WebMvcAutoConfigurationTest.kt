package tech.sergiodelgado.saasstarter.autoconfigure

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.servlet.config.annotation.InterceptorRegistration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isTrue
import tech.sergiodelgado.saasstarter.ratelimit.RateLimitInterceptor
import tech.sergiodelgado.saasstarter.ratelimit.RateLimiter
import tech.sergiodelgado.saasstarter.tenant.TenantInterceptor
import tech.sergiodelgado.saasstarter.tenant.TenantResolver
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

    @Test
    fun `addInterceptors registers rate-limit interceptor on configured path patterns`() {
        val props = SaasStarterProperties(
            rateLimit = SaasStarterProperties.RateLimit(pathPatterns = listOf("/webhooks/**")),
        )
        val rateLimiter = RateLimiter(mockk(relaxed = true))
        val rateLimitInterceptor = RateLimitInterceptor(rateLimiter)
        val rateLimitProvider = interceptorProvider(rateLimitInterceptor)
        val tenantProvider = emptyProvider<TenantInterceptor>()

        val config = WebMvcAutoConfiguration(props, tenantProvider, rateLimitProvider)
        val registry = mockk<InterceptorRegistry>()
        val registration = mockk<InterceptorRegistration>(relaxed = true)
        every { registry.addInterceptor(rateLimitInterceptor) } returns registration

        config.addInterceptors(registry)

        verify { registry.addInterceptor(rateLimitInterceptor) }
    }

    @Test
    fun `addInterceptors registers tenant interceptor with path and exclude patterns`() {
        val props = SaasStarterProperties(
            tenant = SaasStarterProperties.Tenant(
                pathPatterns = listOf("/app/**"),
                excludePathPatterns = listOf("/app/public/**"),
            ),
        )
        val tenantInterceptor = TenantInterceptor(TenantResolver { _ -> UUID.randomUUID() })
        val tenantProvider = interceptorProvider(tenantInterceptor)
        val rateLimitProvider = emptyProvider<RateLimitInterceptor>()

        val config = WebMvcAutoConfiguration(props, tenantProvider, rateLimitProvider)
        val registry = mockk<InterceptorRegistry>()
        val registration = mockk<InterceptorRegistration>(relaxed = true)
        every { registry.addInterceptor(tenantInterceptor) } returns registration
        every { registration.addPathPatterns(*anyVararg<String>()) } returns registration

        config.addInterceptors(registry)

        verify { registry.addInterceptor(tenantInterceptor) }
        verify { registration.excludePathPatterns("/app/public/**") }
    }

    @Test
    fun `addInterceptors skips interceptors when path patterns are empty`() {
        val props = SaasStarterProperties()
        val rateLimitInterceptor = RateLimitInterceptor(RateLimiter(mockk(relaxed = true)))
        val tenantInterceptor = TenantInterceptor(TenantResolver { _ -> UUID.randomUUID() })
        val config = WebMvcAutoConfiguration(
            props,
            interceptorProvider(tenantInterceptor),
            interceptorProvider(rateLimitInterceptor),
        )
        val registry = mockk<InterceptorRegistry>(relaxed = true)

        config.addInterceptors(registry)

        verify(exactly = 0) { registry.addInterceptor(any()) }
    }

    private fun <T : Any> interceptorProvider(value: T): ObjectProvider<T> =
        object : ObjectProvider<T> {
            override fun getObject(): T = value
            override fun getObject(vararg args: Any?): T = value
            override fun getIfAvailable(): T = value
            override fun getIfUnique(): T = value
            override fun iterator(): MutableIterator<T> = mutableListOf(value).iterator()
        }

    private fun <T : Any> emptyProvider(): ObjectProvider<T> =
        object : ObjectProvider<T> {
            override fun getObject(): T = error("no bean")
            override fun getObject(vararg args: Any?): T = error("no bean")
            override fun getIfAvailable(): T? = null
            override fun getIfUnique(): T? = null
            override fun iterator(): MutableIterator<T> = mutableListOf<T>().iterator()
        }
}
