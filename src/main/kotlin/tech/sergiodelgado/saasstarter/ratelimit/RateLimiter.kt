package tech.sergiodelgado.saasstarter.ratelimit

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SessionCallback
import java.time.Duration

/**
 * Sliding window rate limiter backed by Redis.
 *
 * Uses a sorted set per key where each member is a request timestamp.
 * On each request:
 *   1. Remove entries older than the window
 *   2. Count remaining entries
 *   3. If under limit, add current timestamp and allow
 *   4. Otherwise, deny
 */
class RateLimiter(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val observationRegistry: ObservationRegistry = ObservationRegistry.NOOP,
) {

    /**
     * Returns true if the request is allowed, false if rate limit exceeded.
     *
     * @param key      Unique identifier (e.g. "rate:webhook:ip:1.2.3.4")
     * @param limit    Max requests allowed in the window
     * @param window   Time window duration
     */
    fun isAllowed(key: String, limit: Int, window: Duration): Boolean {
        val now = System.currentTimeMillis()
        val windowStart = now - window.toMillis()

        // SessionCallback lets all template ops (opsForZSet, expire) share the same
        // pipelined connection proxy. A plain lambda would be compiled as RedisCallback
        // whose ops each obtain a fresh connection, resulting in an empty pipeline.
        val results = redisTemplate.executePipelined(object : SessionCallback<Unit?> {
            @Suppress("UNCHECKED_CAST")
            override fun <K : Any, V : Any> execute(operations: RedisOperations<K, V>): Unit? {
                val zops = (operations as RedisOperations<String, Any>).opsForZSet()
                zops.removeRangeByScore(key, 0.0, windowStart.toDouble())
                zops.size(key)
                zops.add(key, now.toString(), now.toDouble())
                operations.expire(key, window)
                return null
            }
        })

        val count = (results[1] as? Long) ?: 0
        val allowed = count < limit
        Observation.createNotStarted("saasstarter.ratelimit", observationRegistry)
            .highCardinalityKeyValue("bucket", key)
            .lowCardinalityKeyValue("outcome", if (allowed) "allowed" else "denied")
            .observe { }
        return allowed
    }
}
