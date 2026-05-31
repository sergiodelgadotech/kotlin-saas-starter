package tech.sergiodelgado.saasstarter.observability

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class CorrelationIdFilterTest {

    private val filter = CorrelationIdFilter()

    @AfterEach
    fun cleanup() {
        MDC.remove("correlation_id")
    }

    @Test
    fun `generates correlation ID when X-Correlation-Id header is absent`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        var capturedMdcValue: String? = null

        val chain = FilterChain { _, res ->
            capturedMdcValue = MDC.get("correlation_id")
            val correlationId = capturedMdcValue
            (res as HttpServletResponse).setHeader("X-Correlation-Id", correlationId)
        }

        filter.doFilter(request, response, chain)

        expectThat(capturedMdcValue).isNotNull().isNotEmpty()
        expectThat(response.getHeader("X-Correlation-Id")).isNotNull().isNotEmpty()
    }

    @Test
    fun `uses X-Correlation-Id header value when present`() {
        val existingId = "my-correlation-id-123"
        val request = MockHttpServletRequest().apply {
            addHeader("X-Correlation-Id", existingId)
        }
        val response = MockHttpServletResponse()
        var capturedMdcValue: String? = null

        val chain = FilterChain { _, _ ->
            capturedMdcValue = MDC.get("correlation_id")
        }

        filter.doFilter(request, response, chain)

        expectThat(capturedMdcValue).isEqualTo(existingId)
        expectThat(response.getHeader("X-Correlation-Id")).isEqualTo(existingId)
    }

    @Test
    fun `clears MDC after filter completes normally`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, FilterChain { _, _ -> })

        expectThat(MDC.get("correlation_id")).isNull()
    }

    @Test
    fun `clears MDC even when downstream filter throws`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        runCatching {
            filter.doFilter(request, response, FilterChain { _, _ -> throw RuntimeException("boom") })
        }

        expectThat(MDC.get("correlation_id")).isNull()
    }
}
