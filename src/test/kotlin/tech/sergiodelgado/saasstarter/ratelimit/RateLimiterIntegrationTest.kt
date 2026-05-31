package tech.sergiodelgado.saasstarter.ratelimit

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.data.redis.core.RedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import tech.sergiodelgado.saasstarter.test.TestBootApp
import java.time.Duration
import java.util.UUID

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
class RateLimiterIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val redis = GenericContainer<Nothing>("redis:8.4-alpine").apply {
            withExposedPorts(6379)
        }
    }

    @Autowired
    @Qualifier("jsonRedisTemplate")
    @Suppress("UNCHECKED_CAST")
    lateinit var redisTemplate: RedisTemplate<String, Any>

    private lateinit var rateLimiter: RateLimiter

    @BeforeEach
    fun setUp() {
        rateLimiter = RateLimiter(redisTemplate)
    }

    @Test
    fun `first request under limit is allowed`() {
        val key = "test:first:${UUID.randomUUID()}"
        expectThat(rateLimiter.isAllowed(key, limit = 5, window = Duration.ofMinutes(1))).isTrue()
    }

    @Test
    fun `requests below the limit are all allowed`() {
        val key = "test:below:${UUID.randomUUID()}"
        repeat(4) {
            expectThat(rateLimiter.isAllowed(key, limit = 5, window = Duration.ofMinutes(1))).isTrue()
        }
    }

    @Test
    fun `request that exceeds the limit is denied`() {
        val key = "test:exceed:${UUID.randomUUID()}"
        repeat(5) { rateLimiter.isAllowed(key, limit = 5, window = Duration.ofMinutes(1)) }

        expectThat(rateLimiter.isAllowed(key, limit = 5, window = Duration.ofMinutes(1))).isFalse()
    }

    @Test
    fun `window rollover resets the counter and allows requests again`() {
        val key = "test:rollover:${UUID.randomUUID()}"
        val window = Duration.ofMillis(150)
        // Consume all slots
        repeat(3) { rateLimiter.isAllowed(key, limit = 3, window = window) }
        expectThat(rateLimiter.isAllowed(key, limit = 3, window = window)).isFalse()

        Thread.sleep(200)

        expectThat(rateLimiter.isAllowed(key, limit = 3, window = window)).isTrue()
    }

    @Test
    fun `different keys have independent counters`() {
        val key1 = "test:key1:${UUID.randomUUID()}"
        val key2 = "test:key2:${UUID.randomUUID()}"
        repeat(5) { rateLimiter.isAllowed(key1, limit = 5, window = Duration.ofMinutes(1)) }

        // key2 has not been used; should still be allowed
        expectThat(rateLimiter.isAllowed(key2, limit = 5, window = Duration.ofMinutes(1))).isTrue()
        // key1 is exhausted
        expectThat(rateLimiter.isAllowed(key1, limit = 5, window = Duration.ofMinutes(1))).isFalse()
    }
}
