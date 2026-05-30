package tech.sergiodelgado.saasstarter.autoconfigure

import io.mockk.mockk
import io.mockk.verify
import tech.sergiodelgado.saasstarter.jobs.TenantJobFilter
import org.jobrunr.scheduling.JobScheduler
import org.jobrunr.server.BackgroundJobServer
import org.jobrunr.storage.StorageProvider
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class JobRunrAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(JobRunrAutoConfiguration::class.java))
        .withBean(JobScheduler::class.java, { mockk<JobScheduler>() })

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

    @Test
    fun `job scheduler is registered with tenant filter when no prior scheduler exists`() {
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JobRunrAutoConfiguration::class.java))
            .withBean(StorageProvider::class.java, { mockk<StorageProvider>() })
            .run { context ->
                expectThat(context.getBeansOfType(JobScheduler::class.java)).hasSize(1)
                expectThat(context.getBeansOfType(TenantJobFilter::class.java)).hasSize(1)
            }
    }

    @Test
    fun `tenantJobServerFilterRegistrar adds filter when bean is a BackgroundJobServer`() {
        val filter = TenantJobFilter()
        val processor = JobRunrAutoConfiguration().tenantJobServerFilterRegistrar(filter)
        val server = mockk<BackgroundJobServer>(relaxed = true)

        processor.postProcessBeforeInitialization(server, "backgroundJobServer")

        verify { server.jobFilters }
    }

    @Test
    fun `tenantJobServerFilterRegistrar passes through non-BackgroundJobServer beans unchanged`() {
        val filter = TenantJobFilter()
        val processor = JobRunrAutoConfiguration().tenantJobServerFilterRegistrar(filter)
        val otherBean = Any()

        val result = processor.postProcessBeforeInitialization(otherBean, "someBean")

        expectThat(result).isEqualTo(otherBean)
    }
}
