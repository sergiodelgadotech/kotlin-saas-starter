package org.granchi.saasstarter.autoconfigure

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import strikt.api.expectThat
import strikt.assertions.containsKey
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.time.Duration

class SaasStarterAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SaasStarterAutoConfiguration::class.java))

    @Test
    fun `autoconfig class loads as a bean`() {
        contextRunner.run { context ->
            expectThat(context.getBeansOfType(SaasStarterAutoConfiguration::class.java)).hasSize(1)
        }
    }

    @Test
    fun `properties bean is registered with default enabled=true`() {
        contextRunner.run { context ->
            val props = context.getBean(SaasStarterProperties::class.java)
            expectThat(props.enabled).isTrue()
        }
    }

    @Test
    fun `enabled property can be overridden via configuration`() {
        contextRunner
            .withPropertyValues("saasstarter.enabled=false")
            .run { context ->
                val props = context.getBean(SaasStarterProperties::class.java)
                expectThat(props.enabled).isFalse()
            }
    }

    @Test
    fun `session enabled defaults to true and binds via saasstarter session enabled`() {
        contextRunner.run { context ->
            val props = context.getBean(SaasStarterProperties::class.java)
            expectThat(props.session.enabled).isTrue()
        }

        contextRunner
            .withPropertyValues("saasstarter.session.enabled=false")
            .run { context ->
                val props = context.getBean(SaasStarterProperties::class.java)
                expectThat(props.session.enabled).isFalse()
            }
    }

    @Test
    fun `jobs enabled defaults to true and binds via saasstarter jobs enabled`() {
        contextRunner.run { context ->
            val props = context.getBean(SaasStarterProperties::class.java)
            expectThat(props.jobs.enabled).isTrue()
        }

        contextRunner
            .withPropertyValues("saasstarter.jobs.enabled=false")
            .run { context ->
                val props = context.getBean(SaasStarterProperties::class.java)
                expectThat(props.jobs.enabled).isFalse()
            }
    }

    @Test
    fun `cache configurations bind via saasstarter cache configurations`() {
        contextRunner
            .withPropertyValues(
                "saasstarter.cache.default-ttl=10m",
                "saasstarter.cache.configurations.tenant-by-user.ttl=5m",
                "saasstarter.cache.configurations.organization.ttl=30m",
            )
            .run { context ->
                val props = context.getBean(SaasStarterProperties::class.java)
                expectThat(props.cache.defaultTtl).isEqualTo(Duration.ofMinutes(10))
                expectThat(props.cache.configurations).containsKey("tenant-by-user")
                expectThat(props.cache.configurations["tenant-by-user"]!!.ttl)
                    .isEqualTo(Duration.ofMinutes(5))
                expectThat(props.cache.configurations["organization"]!!.ttl)
                    .isEqualTo(Duration.ofMinutes(30))
            }
    }
}
