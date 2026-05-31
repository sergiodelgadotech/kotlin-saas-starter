package tech.sergiodelgado.saasstarter.observability

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.web.servlet.HandlerInterceptor
import tech.sergiodelgado.saasstarter.tenant.TenantContext

/**
 * Spring MVC interceptor that mirrors the current [TenantContext] into MDC key `tenant_id`.
 *
 * When a tenant is present (i.e. [TenantContext.isPresent] returns true), the tenant ID
 * is written to MDC so it appears in every log line produced within the request's
 * controller and service layers.
 *
 * The MDC entry is always removed in [afterCompletion] to prevent leaks across requests
 * in thread-pooled environments.
 *
 * Note: because this is an interceptor (not a filter), `tenant_id` is available in MDC
 * only after the interceptor chain runs — that is, inside controllers and beans they call.
 * Logs emitted at filter level (earlier in the chain) will not carry `tenant_id`.
 */
class TenantMdcInterceptor : HandlerInterceptor {

    companion object {
        const val MDC_KEY = "tenant_id"
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (TenantContext.isPresent()) {
            MDC.put(MDC_KEY, TenantContext.get().toString())
        }
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        MDC.remove(MDC_KEY)
    }
}
