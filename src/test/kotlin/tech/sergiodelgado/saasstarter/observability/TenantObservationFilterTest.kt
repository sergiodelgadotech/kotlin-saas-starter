package tech.sergiodelgado.saasstarter.observability

import io.micrometer.observation.Observation
import io.micrometer.observation.tck.TestObservationRegistry
import io.micrometer.observation.tck.TestObservationRegistryAssert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import tech.sergiodelgado.saasstarter.tenant.TenantContext
import java.util.UUID

class TenantObservationFilterTest {

    private val filter = TenantObservationFilter()
    private val registry = TestObservationRegistry.create().also {
        it.observationConfig().observationFilter(filter)
    }

    @AfterEach
    fun cleanup() {
        TenantContext.clear()
        registry.clear()
    }

    @Test
    fun `adds tenant_id low-cardinality key-value when TenantContext is present`() {
        val tenantId = UUID.randomUUID()
        TenantContext.set(tenantId)

        val context = Observation.Context()
        val result = filter.map(context)

        expectThat(result.lowCardinalityKeyValues.find { it.key == "tenant.id" }?.value)
            .isEqualTo(tenantId.toString())
    }

    @Test
    fun `does not add tenant_id when TenantContext is absent`() {
        val context = Observation.Context()
        val result = filter.map(context)

        expectThat(result.lowCardinalityKeyValues.find { it.key == "tenant.id" }?.value)
            .isNull()
    }

    @Test
    fun `filter is applied end-to-end when used with TestObservationRegistry`() {
        val tenantId = UUID.randomUUID()
        TenantContext.set(tenantId)

        Observation.createNotStarted("test.observation", registry).observe {}

        TestObservationRegistryAssert.assertThat(registry)
            .hasObservationWithNameEqualTo("test.observation")
            .that()
            .hasLowCardinalityKeyValue("tenant.id", tenantId.toString())
    }
}
