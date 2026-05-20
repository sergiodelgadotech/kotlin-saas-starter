package org.granchi.saasstarter.autoconfigure

import dev.mokkery.mock
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.session.SessionRepository
import strikt.api.expectThat
import strikt.assertions.hasSize

class SessionAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SessionAutoConfiguration::class.java))
        .withBean(RedisConnectionFactory::class.java, { mock<RedisConnectionFactory>() })

    @Test
    fun `autoconfig is loaded when session enabled defaults to true`() {
        contextRunner.run { context ->
            val configs = context.getBeansOfType(SessionAutoConfiguration::class.java)
            expectThat(configs).hasSize(1)
        }
    }

    @Test
    fun `autoconfig is skipped when session enabled is explicitly false`() {
        contextRunner
            .withPropertyValues("saasstarter.session.enabled=false")
            .run { context ->
                val configs = context.getBeansOfType(SessionAutoConfiguration::class.java)
                expectThat(configs).hasSize(0)
            }
    }

    @Test
    fun `autoconfig backs off if a SessionRepository bean already exists`() {
        contextRunner
            .withBean(
                "userDefinedSessionRepository",
                SessionRepository::class.java,
                { object : SessionRepository<org.springframework.session.MapSession> {
                    override fun createSession() = org.springframework.session.MapSession()
                    override fun save(session: org.springframework.session.MapSession) {}
                    override fun findById(id: String) = null
                    override fun deleteById(id: String) {}
                } }
            )
            .run { context ->
                val configs = context.getBeansOfType(SessionAutoConfiguration::class.java)
                expectThat(configs).hasSize(0)
            }
    }
}
