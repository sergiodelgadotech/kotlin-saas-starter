package org.granchi.saasstarter.autoconfigure

import org.granchi.saasstarter.jobs.TenantJobFilter
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import strikt.api.expectThat
import strikt.assertions.hasSize

class JobRunrAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(JobRunrAutoConfiguration::class.java))

    @Test
    fun `tenant job filter bean is registered when enabled`() {
        contextRunner.run { context ->
            val filters = context.getBeansOfType(TenantJobFilter::class.java)
            expectThat(filters).hasSize(1)
        }
    }

    @Test
    fun `autoconfig is skipped when jobs enabled is false`() {
        contextRunner
            .withPropertyValues("saasstarter.jobs.enabled=false")
            .run { context ->
                val filters = context.getBeansOfType(TenantJobFilter::class.java)
                expectThat(filters).hasSize(0)
            }
    }

    @Test
    fun `tenant job filter is overridable by user-defined bean`() {
        contextRunner
            .withBean("userFilter", TenantJobFilter::class.java, { TenantJobFilter() })
            .run { context ->
                val filters = context.getBeansOfType(TenantJobFilter::class.java)
                expectThat(filters).hasSize(1)
            }
    }
}
