package org.granchi.saasstarter.autoconfigure

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isFalse
import strikt.assertions.isTrue

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
}
