package tech.sergiodelgado.saasstarter.observability

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import tech.sergiodelgado.saasstarter.tenant.TenantContext
import java.util.UUID

class TenantMdcInterceptorTest {

    private val interceptor = TenantMdcInterceptor()
    private val request = MockHttpServletRequest()
    private val response = MockHttpServletResponse()
    private val handler = Any()

    @AfterEach
    fun cleanup() {
        TenantContext.clear()
        MDC.remove("tenant_id")
    }

    @Test
    fun `sets tenant_id in MDC when TenantContext is present`() {
        val tenantId = UUID.randomUUID()
        TenantContext.set(tenantId)

        interceptor.preHandle(request, response, handler)

        expectThat(MDC.get("tenant_id")).isEqualTo(tenantId.toString())
    }

    @Test
    fun `does not set tenant_id when TenantContext is absent`() {
        interceptor.preHandle(request, response, handler)

        expectThat(MDC.get("tenant_id")).isNull()
    }

    @Test
    fun `clears tenant_id from MDC in afterCompletion`() {
        val tenantId = UUID.randomUUID()
        TenantContext.set(tenantId)
        MDC.put("tenant_id", tenantId.toString())

        interceptor.afterCompletion(request, response, handler, null)

        expectThat(MDC.get("tenant_id")).isNull()
    }

    @Test
    fun `clears tenant_id from MDC in afterCompletion even when TenantContext is absent`() {
        interceptor.afterCompletion(request, response, handler, null)

        expectThat(MDC.get("tenant_id")).isNull()
    }

    @Test
    fun `clears tenant_id from MDC in afterCompletion when exception is thrown`() {
        val tenantId = UUID.randomUUID()
        TenantContext.set(tenantId)
        MDC.put("tenant_id", tenantId.toString())

        interceptor.afterCompletion(request, response, handler, RuntimeException("boom"))

        expectThat(MDC.get("tenant_id")).isNull()
    }
}
