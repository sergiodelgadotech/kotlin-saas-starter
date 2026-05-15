package org.granchi.saasstarter.autoconfigure

import org.granchi.saasstarter.jobs.JobSchedulerService
import org.granchi.saasstarter.jobs.TenantJobFilter
import org.jobrunr.scheduling.JobScheduler
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Registers JobRunr-related beans:
 * - [TenantJobFilter] propagates tenant context across job lifecycle events and
 *   is auto-discovered by jobrunr-spring-boot-3-starter as a JobFilter bean.
 * - [JobSchedulerService] wraps [JobScheduler] with tenant-context propagation;
 *   only registered when JobRunr's own autoconfig has provided a [JobScheduler].
 *
 * Storage, background job server, and dashboard are configured by
 * jobrunr-spring-boot-3-starter via the standard `org.jobrunr.*` properties.
 *
 * Disabled entirely if `saasstarter.jobs.enabled=false`.
 */
@AutoConfiguration
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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JobScheduler::class)
    fun jobSchedulerService(jobScheduler: JobScheduler): JobSchedulerService =
        JobSchedulerService(jobScheduler)
}
