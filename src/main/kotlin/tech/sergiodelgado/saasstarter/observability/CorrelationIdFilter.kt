package tech.sergiodelgado.saasstarter.observability

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Servlet filter that ensures every request has a correlation ID.
 *
 * Reads the `X-Correlation-Id` request header; if absent, generates a new UUID.
 * Puts the value into MDC key `correlation_id` for structured logging and echoes it
 * back as a response header.
 *
 * MDC is always cleared in a `finally` block to prevent leaks in thread pools.
 */
class CorrelationIdFilter : OncePerRequestFilter() {

    companion object {
        const val HEADER_NAME = "X-Correlation-Id"
        const val MDC_KEY = "correlation_id"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val correlationId = request.getHeader(HEADER_NAME) ?: UUID.randomUUID().toString()
        try {
            MDC.put(MDC_KEY, correlationId)
            response.setHeader(HEADER_NAME, correlationId)
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(MDC_KEY)
        }
    }
}
