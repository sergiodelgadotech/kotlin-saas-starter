package tech.sergiodelgado.saasstarter.observability

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationFilter
import tech.sergiodelgado.saasstarter.tenant.TenantContext

/**
 * Micrometer [ObservationFilter] that tags every observation with the current tenant ID.
 *
 * When [TenantContext.isPresent] returns true, adds `tenant.id` as a low-cardinality
 * key-value on the [Observation.Context]. Low cardinality is appropriate for B2B SaaS
 * products with hundreds or thousands of tenants.
 *
 * **Cardinality warning:** for consumer-scale products where tenants map 1:1 to end-users
 * (millions of users), override this filter and use `highCardinalityKeyValue` instead to
 * avoid metric label explosion.
 */
class TenantObservationFilter : ObservationFilter {

    override fun map(context: Observation.Context): Observation.Context {
        if (TenantContext.isPresent()) {
            context.addLowCardinalityKeyValue(
                io.micrometer.common.KeyValue.of("tenant.id", TenantContext.get().toString())
            )
        }
        return context
    }
}
