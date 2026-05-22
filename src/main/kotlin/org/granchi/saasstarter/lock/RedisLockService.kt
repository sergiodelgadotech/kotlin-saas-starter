package org.granchi.saasstarter.lock

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration
import java.util.UUID

/**
 * Distributed lock backed by Redis SET NX PX.
 *
 * Prevents race conditions in critical operations like:
 * - Creating a Stripe customer for a new organization
 * - Processing a webhook that must run exactly once
 * - Provisioning resources on first login
 */
class RedisLockService(private val redisTemplate: RedisTemplate<String, Any>) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Acquires a lock, executes the block, then releases the lock.
     * If the lock cannot be acquired, throws [LockNotAcquiredException].
     *
     * Usage:
     * ```kotlin
     * lockService.withLock("provision:org:${orgId}") {
     *     // only one instance runs this at a time
     *     stripeService.createCustomer(org)
     * }
     * ```
     */
    fun <T> withLock(key: String, ttl: Duration = Duration.ofSeconds(30), block: () -> T): T {
        val lockKey = "lock:$key"
        val lockValue = UUID.randomUUID().toString()

        val acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, ttl) ?: false

        if (!acquired) {
            log.warn("Could not acquire lock for key: $key")
            throw LockNotAcquiredException("Could not acquire lock: $key")
        }

        return try {
            block()
        } finally {
            // Only release if we still own the lock (value matches)
            val currentValue = redisTemplate.opsForValue().get(lockKey)
            if (currentValue == lockValue) {
                redisTemplate.delete(lockKey)
            }
        }
    }
}

class LockNotAcquiredException(message: String) : RuntimeException(message)
