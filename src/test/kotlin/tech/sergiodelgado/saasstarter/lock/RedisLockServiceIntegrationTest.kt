package tech.sergiodelgado.saasstarter.lock

import io.micrometer.observation.tck.TestObservationRegistry
import io.micrometer.observation.tck.TestObservationRegistryAssert
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.data.redis.core.RedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import tech.sergiodelgado.saasstarter.test.TestBootApp
import java.time.Duration

@Tag("integration")
@Testcontainers
@SpringBootTest(
    classes = [TestBootApp::class],
    properties = [
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
            "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration," +
            "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration," +
            "tech.sergiodelgado.saasstarter.autoconfigure.WebMvcAutoConfiguration," +
            "tech.sergiodelgado.saasstarter.autoconfigure.OrganizationAutoConfiguration," +
            "tech.sergiodelgado.saasstarter.autoconfigure.JobRunrAutoConfiguration," +
            "tech.sergiodelgado.saasstarter.autoconfigure.BillingAutoConfiguration," +
            "tech.sergiodelgado.saasstarter.autoconfigure.WebAutoConfiguration," +
            "org.jobrunr.spring.autoconfigure.JobRunrAutoConfiguration",
        "saasstarter.session.enabled=false",
    ],
)
class RedisLockServiceIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val redis = GenericContainer<Nothing>("redis:8.4-alpine").apply {
            withExposedPorts(6379)
        }
    }

    @Autowired
    lateinit var lockService: RedisLockService

    @Autowired
    @Qualifier("jsonRedisTemplate")
    @Suppress("UNCHECKED_CAST")
    lateinit var redisTemplate: RedisTemplate<String, Any>

    @Test
    fun `releases lock key after block completes`() {
        lockService.withLock("release-test") { /* no-op */ }

        expectThat(redisTemplate.opsForValue().get("lock:release-test")).isNull()
    }

    @Test
    fun `does not delete lock key held by a different owner`() {
        val lockKey = "lock:foreign-owner-test"

        lockService.withLock("foreign-owner-test", ttl = Duration.ofMillis(100)) {
            Thread.sleep(200) // let the lock TTL expire
            // Simulate another holder acquiring the lock
            redisTemplate.opsForValue().set(lockKey, "foreign-uuid", Duration.ofSeconds(30))
        }

        // The original holder's finally block must NOT have deleted the new owner's lock
        expectThat(redisTemplate.opsForValue().get(lockKey)).isEqualTo("foreign-uuid")
    }

    @Test
    fun `throws LockNotAcquiredException when lock is already held`() {
        lockService.withLock("contention-test", ttl = Duration.ofSeconds(30)) {
            assertThrows<LockNotAcquiredException> {
                lockService.withLock("contention-test", ttl = Duration.ofSeconds(30)) {}
            }
        }
    }

    @Test
    fun `withLock records successful lock observation`() {
        val observationRegistry = TestObservationRegistry.create()
        val service = RedisLockService(redisTemplate, observationRegistry)

        service.withLock("obs-success-test") { /* no-op */ }

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasObservationWithNameEqualTo("saasstarter.lock")
            .that()
            .hasLowCardinalityKeyValue("operation", "acquire")
            .hasLowCardinalityKeyValue("outcome", "success")
            .hasHighCardinalityKeyValue("lock.key", "obs-success-test")
    }

    @Test
    fun `withLock records contended observation when lock is already held`() {
        val observationRegistry = TestObservationRegistry.create()
        val service = RedisLockService(redisTemplate, observationRegistry)

        service.withLock("obs-contended-test", ttl = Duration.ofSeconds(30)) {
            assertThrows<LockNotAcquiredException> {
                service.withLock("obs-contended-test") { }
            }
        }

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasAnObservationWithAKeyValue("outcome", "contended")
    }

    @Test
    fun `withLock records error observation when block throws`() {
        val observationRegistry = TestObservationRegistry.create()
        val service = RedisLockService(redisTemplate, observationRegistry)

        assertThrows<RuntimeException> {
            service.withLock("obs-error-test") { throw RuntimeException("block error") }
        }

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasObservationWithNameEqualTo("saasstarter.lock")
            .that()
            .hasLowCardinalityKeyValue("operation", "acquire")
            .hasLowCardinalityKeyValue("outcome", "error")
            .hasHighCardinalityKeyValue("lock.key", "obs-error-test")
    }

    @Test
    fun `withLock records release observation after successful block`() {
        val observationRegistry = TestObservationRegistry.create()
        val service = RedisLockService(redisTemplate, observationRegistry)

        service.withLock("obs-release-test") { /* no-op */ }

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasAnObservationWithAKeyValue("operation", "release")
        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasAnObservationWithAKeyValue("outcome", "released")
        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasAnObservationWithAKeyValue("lock.key", "obs-release-test")
    }
}
