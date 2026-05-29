package tech.sergiodelgado.saasstarter.lock

import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.output.IntegerOutput
import io.lettuce.core.protocol.CommandArgs
import io.lettuce.core.protocol.ProtocolKeyword
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisCallback
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import java.time.Duration
import java.util.UUID

/**
 * Distributed lock backed by Redis SET NX PX / DELEX IFEQ.
 *
 * Requires **Redis 8.4+** — lock release uses the native `DELEX key IFEQ value`
 * command (GA November 2025) for an atomic compare-and-delete in a single round
 * trip, preventing a second holder's lock from being deleted when the first
 * holder's TTL expires mid-work.
 *
 * Prevents race conditions in critical operations like:
 * - Creating a Stripe customer for a new organization
 * - Processing a webhook that must run exactly once
 * - Provisioning resources on first login
 */
class RedisLockService(private val redisTemplate: RedisTemplate<String, Any>) {

    private val log = LoggerFactory.getLogger(javaClass)

    private companion object {
        // ProtocolKeyword for DELEX, bypassing Lettuce's CommandType enum (DELEX is Redis 8.4+)
        val DELEX: ProtocolKeyword = ProtocolKeyword { "DELEX".toByteArray(Charsets.US_ASCII) }
    }

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
            @Suppress("UNCHECKED_CAST")
            val keyBytes = (redisTemplate.keySerializer as RedisSerializer<String>).serialize(lockKey)!!
            @Suppress("UNCHECKED_CAST")
            val valueBytes = (redisTemplate.valueSerializer as RedisSerializer<Any>).serialize(lockValue)!!
            redisTemplate.execute(RedisCallback { connection ->
                // Spring Data Redis's generic execute() cannot decode DELEX's integer reply,
                // so we use Lettuce's dispatch() with IntegerOutput directly.
                // nativeConnection returns RedisAsyncCommandsImpl in Spring Data Redis + Lettuce.
                val codec = ByteArrayCodec.INSTANCE
                @Suppress("UNCHECKED_CAST")
                val lettuce = connection.nativeConnection as RedisAsyncCommands<ByteArray, ByteArray>
                lettuce.dispatch(
                    DELEX,
                    IntegerOutput(codec),
                    CommandArgs(codec).addKey(keyBytes).add("IFEQ").addValue(valueBytes),
                ).get()
            })
        }
    }
}

class LockNotAcquiredException(message: String) : RuntimeException(message)
