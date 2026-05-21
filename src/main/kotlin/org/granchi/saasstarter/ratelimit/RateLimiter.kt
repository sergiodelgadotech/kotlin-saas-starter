package org.granchi.saasstarter.ratelimit

import org.springframework.data.redis.core.RedisTemplate
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
class RateLimiter(private val redisTemplate: RedisTemplate<String, Any>) {

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
        val ops = redisTemplate.opsForZSet()

        // Atomic sliding window via pipeline
        val results = redisTemplate.executePipelined {
            ops.removeRangeByScore(key, 0.0, windowStart.toDouble())
            ops.size(key)
            ops.add(key, now.toString(), now.toDouble())
            redisTemplate.expire(key, window)
        }

        val count = (results[1] as? Long) ?: 0
        return count < limit
    }
}
