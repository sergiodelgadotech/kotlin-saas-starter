package org.granchi.saasstarter.jobs

import org.granchi.saasstarter.tenant.TenantContext
import org.jobrunr.jobs.AbstractJob
import org.jobrunr.jobs.Job
import org.jobrunr.jobs.filters.JobClientFilter
import org.jobrunr.jobs.filters.JobServerFilter
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Jobrunr filter that propagates the current tenant ID through a job's
 * lifecycle: it tags the job with a `tenant:<uuid>` label on creation and
 * restores the tenant on the worker before the job runs.
 *
 * The tenant label is also visible in the JobRunr dashboard, which makes
 * per-tenant filtering of jobs a useful side effect.
 *
 * Note: a single job may carry at most one tenant label. JobRunr caps a
 * job's labels at 3 × 45 chars; `tenant:` + a UUID is 43 chars and fits.
 */
@Component
class TenantJobFilter : JobClientFilter, JobServerFilter {

    override fun onCreating(job: AbstractJob) {
        if (TenantContext.isPresent()) {
            job.labels = job.labels + "$TENANT_LABEL_PREFIX${TenantContext.get()}"
        }
    }

    override fun onProcessing(job: Job) {
        val tenantId = job.labels
            .firstOrNull { it.startsWith(TENANT_LABEL_PREFIX) }
            ?.removePrefix(TENANT_LABEL_PREFIX)
            ?: return

        try {
            TenantContext.set(UUID.fromString(tenantId))
        } catch (_: IllegalArgumentException) {
            // Label was malformed; leave context unset rather than poisoning it.
        }
    }

    override fun onProcessingSucceeded(job: Job) {
        TenantContext.clear()
    }

    override fun onProcessingFailed(job: Job, e: Exception) {
        TenantContext.clear()
    }

    companion object {
        private const val TENANT_LABEL_PREFIX = "tenant:"
    }
}
