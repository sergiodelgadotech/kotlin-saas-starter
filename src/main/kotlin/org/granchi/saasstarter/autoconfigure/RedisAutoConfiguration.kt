package org.granchi.saasstarter.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
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
 */
@AutoConfiguration
@ConditionalOnClass(RedisConnectionFactory::class)
@ConditionalOnBean(RedisConnectionFactory::class)
@ConditionalOnProperty(
    prefix = "saasstarter.cache",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@EnableConfigurationProperties(SaasStarterProperties::class)
@EnableCaching
class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun redisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, Any> =
        RedisTemplate<String, Any>().apply {
            connectionFactory = factory
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = GenericJackson2JsonRedisSerializer()
        }

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
                    .fromSerializer(GenericJackson2JsonRedisSerializer())
            )
}
