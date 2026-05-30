package tech.sergiodelgado.saasstarter.autoconfigure

import tech.sergiodelgado.saasstarter.ratelimit.RateLimitInterceptor
import tech.sergiodelgado.saasstarter.ratelimit.RateLimiter
import tech.sergiodelgado.saasstarter.ratelimit.RouteConfig
import tech.sergiodelgado.saasstarter.tenant.TenantInterceptor
import tech.sergiodelgado.saasstarter.tenant.TenantResolver
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Wires TenantInterceptor, RateLimitInterceptor, and RateLimiter as beans and
 * registers them on path patterns from saasstarter.tenant.* and
 * saasstarter.rate-limit.* properties.
 *
 * [TenantInterceptor] is declared if saasstarter.tenant.enabled=true (default)
 * and a [TenantResolver] bean is available.
 * [RateLimitInterceptor] is declared if saasstarter.rate-limit.enabled=true (default).
 * [RateLimiter] is declared when a RedisTemplate<String, Any> bean is present.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(WebMvcConfigurer::class)
@EnableConfigurationProperties(SaasStarterProperties::class)
class WebMvcAutoConfiguration(
    private val properties: SaasStarterProperties,
    private val tenantInterceptorProvider: ObjectProvider<TenantInterceptor>,
    private val rateLimitInterceptorProvider: ObjectProvider<RateLimitInterceptor>,
) : WebMvcConfigurer {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedisTemplate::class)
    fun rateLimiter(
        @Suppress("UNCHECKED_CAST")
        redisTemplate: RedisTemplate<String, Any>,
    ): RateLimiter = RateLimiter(redisTemplate)

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RateLimiter::class)
    @ConditionalOnProperty(
        prefix = "saasstarter.rate-limit",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true,
    )
    fun rateLimitInterceptor(rateLimiter: RateLimiter): RateLimitInterceptor =
        RateLimitInterceptor(
            rateLimiter = rateLimiter,
            defaultLimit = properties.rateLimit.default.limit,
            defaultWindow = properties.rateLimit.default.window,
            routes = properties.rateLimit.routes.map { RouteConfig(it.pathPattern, it.limit, it.window) },
        )

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(TenantResolver::class)
    @ConditionalOnProperty(
        prefix = "saasstarter.tenant",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true,
    )
    fun tenantInterceptor(tenantResolver: TenantResolver): TenantInterceptor =
        TenantInterceptor(tenantResolver)

    override fun addInterceptors(registry: InterceptorRegistry) {
        rateLimitInterceptorProvider.ifAvailable { interceptor ->
            if (properties.rateLimit.pathPatterns.isNotEmpty()) {
                val registration = registry.addInterceptor(interceptor)
                    .addPathPatterns(*properties.rateLimit.pathPatterns.toTypedArray())
                if (properties.rateLimit.excludePathPatterns.isNotEmpty()) {
                    registration.excludePathPatterns(*properties.rateLimit.excludePathPatterns.toTypedArray())
                }
            }
        }
        tenantInterceptorProvider.ifAvailable { interceptor ->
            if (properties.tenant.pathPatterns.isNotEmpty()) {
                val registration = registry.addInterceptor(interceptor)
                    .addPathPatterns(*properties.tenant.pathPatterns.toTypedArray())
                if (properties.tenant.excludePathPatterns.isNotEmpty()) {
                    registration.excludePathPatterns(*properties.tenant.excludePathPatterns.toTypedArray())
                }
            }
        }
    }
}
