package tech.sergiodelgado.saasstarter.jobs

import io.micrometer.observation.tck.TestObservationRegistry
import io.micrometer.observation.tck.TestObservationRegistryAssert
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jobrunr.jobs.JobId
import org.jobrunr.jobs.lambdas.JobLambda
import org.jobrunr.scheduling.JobScheduler
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Duration
import java.time.Instant
import java.util.UUID

class JobSchedulerServiceTest {

    private val jobScheduler = mockk<JobScheduler>()
    private val service = JobSchedulerService(jobScheduler)

    @Test
    fun `enqueue returns the UUID from the JobId`() {
        val expectedId = UUID.randomUUID()
        val jobId = mockk<JobId>()
        val job = mockk<JobLambda>()
        every { jobId.asUUID() } returns expectedId
        every { jobScheduler.enqueue(job) } returns jobId

        val result = service.enqueue(job)

        expectThat(result).isEqualTo(expectedId)
        verify { jobScheduler.enqueue(job) }
    }

    @Test
    fun `schedule returns the UUID from the JobId`() {
        val expectedId = UUID.randomUUID()
        val jobId = mockk<JobId>()
        val runAt = Instant.now().plusSeconds(60)
        val job = mockk<JobLambda>()
        every { jobId.asUUID() } returns expectedId
        every { jobScheduler.schedule(runAt, job) } returns jobId

        val result = service.schedule(runAt, job)

        expectThat(result).isEqualTo(expectedId)
        verify { jobScheduler.schedule(runAt, job) }
    }

    @Test
    fun `scheduleIn delegates to schedule with an instant in the future`() {
        val expectedId = UUID.randomUUID()
        val jobId = mockk<JobId>()
        val delay = Duration.ofSeconds(120)
        val job = mockk<JobLambda>()
        every { jobId.asUUID() } returns expectedId
        every { jobScheduler.schedule(any<Instant>(), eq(job)) } returns jobId

        val result = service.scheduleIn(delay, job)

        expectThat(result).isEqualTo(expectedId)
        verify { jobScheduler.schedule(any<Instant>(), job) }
    }

    @Test
    fun `enqueue records observation with job id`() {
        val observationRegistry = TestObservationRegistry.create()
        val observedService = JobSchedulerService(jobScheduler, observationRegistry)
        val expectedId = UUID.randomUUID()
        val jobId = mockk<JobId>()
        val job = mockk<JobLambda>()
        every { jobId.asUUID() } returns expectedId
        every { jobScheduler.enqueue(job) } returns jobId

        observedService.enqueue(job)

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasObservationWithNameEqualTo("saasstarter.job")
            .that()
            .hasLowCardinalityKeyValue("operation", "enqueue")
            .hasHighCardinalityKeyValue("job.id", expectedId.toString())
    }

    @Test
    fun `schedule records observation with job id`() {
        val observationRegistry = TestObservationRegistry.create()
        val observedService = JobSchedulerService(jobScheduler, observationRegistry)
        val expectedId = UUID.randomUUID()
        val jobId = mockk<JobId>()
        val runAt = Instant.now().plusSeconds(60)
        val job = mockk<JobLambda>()
        every { jobId.asUUID() } returns expectedId
        every { jobScheduler.schedule(runAt, job) } returns jobId

        observedService.schedule(runAt, job)

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasObservationWithNameEqualTo("saasstarter.job")
            .that()
            .hasLowCardinalityKeyValue("operation", "schedule")
            .hasHighCardinalityKeyValue("job.id", expectedId.toString())
    }
}
