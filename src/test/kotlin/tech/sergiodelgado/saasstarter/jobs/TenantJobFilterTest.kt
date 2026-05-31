package tech.sergiodelgado.saasstarter.jobs

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jobrunr.jobs.AbstractJob
import org.jobrunr.jobs.Job
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import tech.sergiodelgado.saasstarter.tenant.TenantContext
import java.util.UUID

class TenantJobFilterTest {

    private val filter = TenantJobFilter()

    @AfterEach
    fun cleanup() {
        TenantContext.clear()
    }

    // ─── onCreating ──────────────────────────────────────────────────────────

    @Test
    fun `onCreating adds tenant label when tenant context is present`() {
        val tenantId = UUID.randomUUID()
        TenantContext.set(tenantId)
        val job = mockk<AbstractJob>(relaxed = true)
        every { job.labels } returns emptyList()

        filter.onCreating(job)

        verify { job.labels = listOf("tenant:$tenantId") }
    }

    @Test
    fun `onCreating does not modify labels when tenant context is absent`() {
        val job = mockk<AbstractJob>(relaxed = true)

        filter.onCreating(job)

        verify(exactly = 0) { job.labels = any() }
    }

    @Test
    fun `onCreating preserves existing labels`() {
        val tenantId = UUID.randomUUID()
        TenantContext.set(tenantId)
        val job = mockk<AbstractJob>(relaxed = true)
        every { job.labels } returns listOf("existing-label")

        filter.onCreating(job)

        verify { job.labels = listOf("existing-label", "tenant:$tenantId") }
    }

    // ─── onProcessing ────────────────────────────────────────────────────────

    @Test
    fun `onProcessing restores tenant context from label`() {
        val tenantId = UUID.randomUUID()
        val job = mockk<Job>(relaxed = true)
        every { job.labels } returns listOf("tenant:$tenantId")

        filter.onProcessing(job)

        expectThat(TenantContext.isPresent()).isTrue()
        expectThat(TenantContext.get()).isEqualTo(tenantId)
    }

    @Test
    fun `onProcessing does nothing when no tenant label is present`() {
        val job = mockk<Job>(relaxed = true)
        every { job.labels } returns emptyList()

        filter.onProcessing(job)

        expectThat(TenantContext.isPresent()).isFalse()
    }

    @Test
    fun `onProcessing ignores malformed UUID label without throwing`() {
        val job = mockk<Job>(relaxed = true)
        every { job.labels } returns listOf("tenant:not-a-uuid")

        filter.onProcessing(job)

        expectThat(TenantContext.isPresent()).isFalse()
    }

    // ─── onProcessingSucceeded / onProcessingFailed ──────────────────────────

    @Test
    fun `onProcessingSucceeded clears tenant context`() {
        TenantContext.set(UUID.randomUUID())
        val job = mockk<Job>(relaxed = true)

        filter.onProcessingSucceeded(job)

        expectThat(TenantContext.isPresent()).isFalse()
    }

    @Test
    fun `onProcessingFailed clears tenant context`() {
        TenantContext.set(UUID.randomUUID())
        val job = mockk<Job>(relaxed = true)

        filter.onProcessingFailed(job, RuntimeException("simulated failure"))

        expectThat(TenantContext.isPresent()).isFalse()
    }
}
