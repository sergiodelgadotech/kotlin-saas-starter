package org.granchi.saasstarter.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.session.SessionRepository
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession

/**
 * Stores HTTP sessions in Redis with a 24h inactivity timeout, allowing
 * horizontal scaling without sticky sessions.
 *
 * Disabled if either:
 * - `saasstarter.session.enabled=false`, or
 * - the consumer defines its own [SessionRepository] bean.
 *
 * Runs after [RedisAutoConfiguration] so the [RedisConnectionFactory] bean is
 * available when [EnableRedisSessionConfig]'s [ConditionalOnBean] is evaluated.
 */
@AutoConfiguration
@AutoConfigureAfter(RedisAutoConfiguration::class)
@ConditionalOnClass(EnableRedisHttpSession::class)
@ConditionalOnProperty(
    prefix = "saasstarter.session",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@ConditionalOnMissingBean(SessionRepository::class)
class SessionAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(RedisConnectionFactory::class)
    @EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400)
    class EnableRedisSessionConfig
}
