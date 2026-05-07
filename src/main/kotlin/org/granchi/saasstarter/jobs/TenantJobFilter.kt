package org.granchi.saasstarter.jobs

import org.granchi.saasstarter.tenant.TenantContext
import org.jobrunr.jobs.filters.JobClientFilter
import org.jobrunr.jobs.filters.JobServerFilter
import org.jobrunr.jobs.Job
import org.jobrunr.jobs.context.JobContext
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Jobrunr filter that stores the current tenant ID in the job's
 * metadata before enqueueing, and restores it when the job runs.
 *
 * This allows services inside a job to call TenantContext.get()
 * as if they were in a normal HTTP request.
 */
@Component
class TenantJobFilter : JobClientFilter, JobServerFilter {

    companion object {
        private const val TENANT_ID_KEY = "tenantId"
    }

    // Called before the job is saved — store current tenant
    override fun onCreating(job: Job) {
        if (TenantContext.isPresent()) {
            job.jobDetails.addJobParameter(
                TENANT_ID_KEY,
                TenantContext.get().toString()
            )
        }
    }

    // Called before the job runs — restore tenant
    override fun onProcessing(job: Job) {
        val tenantIdStr = job.jobDetails.jobParameters
            .find { it.className == "java.lang.String" && it.`object` is String }
            ?.`object` as? String
            ?: return

        try {
            TenantContext.set(UUID.fromString(tenantIdStr))
        } catch (e: IllegalArgumentException) {
            // Not a UUID — job was enqueued without tenant context (e.g. system job)
        }
    }

    // Called after the job completes — clean up
    override fun onProcessed(job: Job) {
        TenantContext.clear()
    }

    override fun onCreated(job: Job) {}
}
