package tech.sergiodelgado.saasstarter.jobs

import tech.sergiodelgado.saasstarter.tenant.TenantContext
import org.jobrunr.jobs.lambdas.JobLambda
import org.jobrunr.scheduling.JobScheduler
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Thin wrapper around Jobrunr's JobScheduler.
 *
 * Automatically propagates the current tenant ID into every job
 * so that services inside the job can call TenantContext.get()
 * without extra boilerplate.
 *
 * Usage:
 * ```kotlin
 * jobSchedulerService.enqueue { fileProcessor.process(fileId) }
 * jobSchedulerService.schedule(Instant.now().plusSeconds(60)) { reminderService.send(userId) }
 * ```
 */
class JobSchedulerService(private val jobScheduler: JobScheduler) {

    fun enqueue(job: JobLambda): UUID =
        jobScheduler.enqueue(job).asUUID()

    fun schedule(runAt: Instant, job: JobLambda): UUID =
        jobScheduler.schedule(runAt, job).asUUID()

    fun scheduleIn(delay: Duration, job: JobLambda): UUID =
        schedule(Instant.now().plus(delay), job)
}
