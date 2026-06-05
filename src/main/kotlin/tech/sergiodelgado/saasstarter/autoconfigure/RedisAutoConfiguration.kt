package tech.sergiodelgado.saasstarter.autoconfigure

import io.micrometer.observation.ObservationRegistry
import tech.sergiodelgado.saasstarter.lock.RedisLockService
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * Redis-backed caching with String keys and JSON values.
 *
 * Provides [RedisTemplate]<String, Any> and a [RedisCacheManager] built from
 * [SaasStarterProperties.Cache.configurations]. Disabled by
 * `saasstarter.cache.enabled=false`. Backs off if the consumer defines its own
 * `RedisTemplate` or `RedisCacheManager`.
 *
 * Follows the same pattern as [SessionAutoConfiguration]: outer class gates on
 * class-path and property; inner [BeansConfig] gates on [RedisConnectionFactory]
 * presence. This is necessary because class-level @ConditionalOnBean on an
 * @AutoConfiguration class is evaluated before other autoconfig @Bean methods
 * are registered — using @AutoConfigureAfter with an inner @Configuration class
 * is the correct approach (same as Spring Boot's own RedisCacheConfiguration).
 */
@AutoConfiguration
@AutoConfigureAfter(name = ["org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration"])
@ConditionalOnClass(RedisConnectionFactory::class)
@ConditionalOnProperty(
    prefix = "saasstarter.cache",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@EnableConfigurationProperties(SaasStarterProperties::class)
@EnableCaching
class RedisAutoConfiguration {

    @Suppress("DEPRECATION") // GenericJackson2JsonRedisSerializer deprecated in SDR 4.x; see jsonSerializer() comment
    @Configuration(proxyBeanMethods = false)
    class BeansConfig {

        @Bean("jsonRedisTemplate")
        @ConditionalOnMissingBean(name = ["jsonRedisTemplate"])
        fun jsonRedisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, Any> =
            RedisTemplate<String, Any>().apply {
                connectionFactory = factory
                keySerializer = StringRedisSerializer()
                valueSerializer = jsonSerializer()
                hashKeySerializer = StringRedisSerializer()
                hashValueSerializer = jsonSerializer()
            }

        @Bean
        @ConditionalOnMissingBean
        fun redisLockService(
            @Qualifier("jsonRedisTemplate")
            @Suppress("UNCHECKED_CAST")
            redisTemplate: RedisTemplate<String, Any>,
            observationRegistry: ObjectProvider<ObservationRegistry>,
        ): RedisLockService = RedisLockService(redisTemplate, observationRegistry.getIfAvailable { ObservationRegistry.NOOP })

        @Bean
        @ConditionalOnMissingBean
        fun cacheManager(
            factory: RedisConnectionFactory,
            properties: SaasStarterProperties,
        ): RedisCacheManager {
            val defaults = baseConfig().entryTtl(properties.cache.defaultTtl)
            val perCache = properties.cache.configurations.mapValues { (_, entry) ->
                entry.ttl?.let { defaults.entryTtl(it) } ?: defaults
            }
            return RedisCacheManager.builder(factory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .build()
        }

        private fun baseConfig(): RedisCacheConfiguration =
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(StringRedisSerializer())
                )
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer())
                )

        // The no-arg constructor uses NON_FINAL typing, which excludes final classes like
        // UUID: cache hits deserialize them as String, causing a ClassCastException in the
        // JDK repository proxy. The builder's defaultTyping(true) installs Spring Data
        // Redis's TypeResolverBuilder (configured with EVERYTHING typing), so final-class
        // values carry @class metadata and round-trip correctly.
        // GenericJackson2JsonRedisSerializer is deprecated in SDR 4.x in favour of the
        // Jackson 3.x variant, but that variant dropped EVERYTHING typing; the 2.x class
        // is still on the runtime classpath (jackson-databind 2.21.x) so this is safe.
        @Suppress("DEPRECATION")
        private fun jsonSerializer(): GenericJackson2JsonRedisSerializer =
            GenericJackson2JsonRedisSerializer.builder()
                .defaultTyping(true)
                .build()
    }
}
