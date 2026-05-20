package org.granchi.saasstarter.autoconfigure

import dev.mokkery.mock
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isA

class RedisAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(RedisAutoConfiguration::class.java))
        .withBean(RedisConnectionFactory::class.java, { mock<RedisConnectionFactory>() })

    @Test
    fun `RedisTemplate is registered with String key and JSON value serializers`() {
        contextRunner.run { context ->
            @Suppress("UNCHECKED_CAST")
            val template = context.getBean(RedisTemplate::class.java) as RedisTemplate<String, Any>
            expectThat(template.keySerializer).isA<StringRedisSerializer>()
            expectThat(template.valueSerializer).isA<GenericJackson2JsonRedisSerializer>()
        }
    }

    @Test
    fun `RedisCacheManager is registered`() {
        contextRunner.run { context ->
            val managers = context.getBeansOfType(RedisCacheManager::class.java)
            expectThat(managers).hasSize(1)
        }
    }

    @Test
    fun `cache manager honours per-cache TTLs from configurations property`() {
        contextRunner
            .withPropertyValues(
                "saasstarter.cache.default-ttl=10m",
                "saasstarter.cache.configurations.tenant-by-user.ttl=5m",
            )
            .run { context ->
                val manager = context.getBean(RedisCacheManager::class.java)
                expectThat(manager.cacheNames).contains("tenant-by-user")
            }
    }

    @Test
    fun `autoconfig is skipped when cache enabled is false`() {
        contextRunner
            .withPropertyValues("saasstarter.cache.enabled=false")
            .run { context ->
                expectThat(context.getBeansOfType(RedisCacheManager::class.java)).hasSize(0)
            }
    }
}
