package tech.sergiodelgado.saasstarter.observability

import io.micrometer.common.KeyValue
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationFilter
import org.springframework.http.server.observation.ServerRequestObservationContext
import tech.sergiodelgado.saasstarter.tenant.TenantContext

/**
 * Micrometer [ObservationFilter] that tags every observation with the current tenant ID.
 *
 * Two resolution paths:
 * 1. [TenantContext.isPresent] — used for non-HTTP observations (lock, job, rate limit, webhook).
 * 2. `ServerRequestObservationContext.carrier` attribute — used for HTTP server observations,
 *    where [TenantContext] may already be cleared by `TenantInterceptor.afterCompletion()` before
 *    `ServerHttpObservationFilter` stops the observation in its finally block.
 *    [TenantInterceptor] writes [TenantContext.REQUEST_ATTRIBUTE] on the request so the tenant ID
 *    survives for the full duration of the filter chain.
 *
 * Low cardinality is appropriate for B2B SaaS with hundreds-to-thousands of tenants.
 * For consumer-scale products (millions of users), use `highCardinalityKeyValue` instead.
 */
class TenantObservationFilter : ObservationFilter {

    override fun map(context: Observation.Context): Observation.Context {
        val tenantId = when {
            TenantContext.isPresent() -> TenantContext.get().toString()
            context is ServerRequestObservationContext ->
                context.carrier?.getAttribute(TenantContext.REQUEST_ATTRIBUTE)?.toString()
            else -> null
        }
        if (tenantId != null) {
            context.addLowCardinalityKeyValue(KeyValue.of("tenant.id", tenantId))
        }
        return context
    }
}
