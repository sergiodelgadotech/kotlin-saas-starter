package tech.sergiodelgado.saasstarter.jobs

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
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
class JobSchedulerService(
    private val jobScheduler: JobScheduler,
    private val observationRegistry: ObservationRegistry = ObservationRegistry.NOOP,
) {

    fun enqueue(job: JobLambda): UUID {
        val obs = Observation.createNotStarted("saasstarter.job", observationRegistry)
            .lowCardinalityKeyValue("operation", "enqueue")
            .start()
        return try {
            jobScheduler.enqueue(job).asUUID().also { jobId ->
                obs.highCardinalityKeyValue("job.id", jobId.toString())
            }
        } finally {
            obs.stop()
        }
    }

    fun schedule(runAt: Instant, job: JobLambda): UUID {
        val obs = Observation.createNotStarted("saasstarter.job", observationRegistry)
            .lowCardinalityKeyValue("operation", "schedule")
            .start()
        return try {
            jobScheduler.schedule(runAt, job).asUUID().also { jobId ->
                obs.highCardinalityKeyValue("job.id", jobId.toString())
            }
        } finally {
            obs.stop()
        }
    }

    fun scheduleIn(delay: Duration, job: JobLambda): UUID =
        schedule(Instant.now().plus(delay), job)
}
