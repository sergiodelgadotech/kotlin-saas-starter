package tech.sergiodelgado.saasstarter.autoconfigure

import io.micrometer.observation.ObservationRegistry
import tech.sergiodelgado.saasstarter.jobs.JobSchedulerService
import tech.sergiodelgado.saasstarter.jobs.TenantJobFilter
import org.jobrunr.scheduling.JobScheduler
import org.jobrunr.server.BackgroundJobServer
import org.jobrunr.storage.StorageProvider
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Registers JobRunr-related beans and wires [TenantJobFilter] into both the
 * client scheduler and the background job server.
 *
 * Runs before jobrunr-spring-boot-3-starter's own [JobRunrAutoConfiguration]
 * so that our [jobScheduler] bean (with [TenantJobFilter] in its filter list)
 * satisfies the starter's @ConditionalOnMissingBean and is used as-is.
 * The [tenantJobServerFilterRegistrar] BeanPostProcessor then injects the filter
 * into [BackgroundJobServer] before its start() is called.
 *
 * Storage, background job server, and dashboard are configured by
 * jobrunr-spring-boot-3-starter via the standard `org.jobrunr.*` properties.
 *
 * Disabled entirely if `saasstarter.jobs.enabled=false`.
 */
@AutoConfiguration
@AutoConfigureBefore(name = ["org.jobrunr.spring.autoconfigure.JobRunrAutoConfiguration"])
@ConditionalOnClass(JobScheduler::class)
@ConditionalOnProperty(
    prefix = "saasstarter.jobs",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@EnableConfigurationProperties(SaasStarterProperties::class)
class JobRunrAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun tenantJobFilter(): TenantJobFilter = TenantJobFilter()

    // Provides JobScheduler with TenantJobFilter in its client-side filter list.
    // The starter's own jobScheduler bean has @ConditionalOnMissingBean, so it
    // defers to this one when our autoconfig runs first.
    @Bean
    @ConditionalOnMissingBean
    fun jobScheduler(storageProvider: StorageProvider, tenantJobFilter: TenantJobFilter): JobScheduler =
        JobScheduler(storageProvider, listOf(tenantJobFilter))

    @Bean
    @ConditionalOnMissingBean
    fun jobSchedulerService(
        jobScheduler: JobScheduler,
        observationRegistry: ObjectProvider<ObservationRegistry>,
    ): JobSchedulerService =
        JobSchedulerService(jobScheduler, observationRegistry.getIfAvailable { ObservationRegistry.NOOP })

    // Adds TenantJobFilter to BackgroundJobServer's server-side filter list.
    // postProcessBeforeInitialization fires before initMethod="start", so the
    // filter is in place before the server begins polling.
    @Bean
    fun tenantJobServerFilterRegistrar(tenantJobFilter: TenantJobFilter): BeanPostProcessor =
        object : BeanPostProcessor {
            override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
                if (bean is BackgroundJobServer) {
                    bean.jobFilters.addAll(listOf(tenantJobFilter))
                }
                return bean
            }
        }
}
