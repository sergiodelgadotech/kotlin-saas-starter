package tech.sergiodelgado.saasstarter.tenant

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.util.UUID

class TenantInterceptorTest {

    private val tenantResolver = mockk<TenantResolver>()
    private val interceptor = TenantInterceptor(tenantResolver)
    // JwtAuthFilter.getUserId reads request.getAttribute("auth_user_id"), so we mock the request.
    private val request = mockk<HttpServletRequest>(relaxed = true)
    private val response = mockk<HttpServletResponse>(relaxed = true)
    private val handler = Any()

    @AfterEach
    fun cleanup() {
        TenantContext.clear()
    }

    @Test
    fun `preHandle returns true and sets no context when no user id in request`() {
        every { request.getAttribute("auth_user_id") } returns null

        val result = interceptor.preHandle(request, response, handler)

        expectThat(result).isTrue()
        expectThat(TenantContext.isPresent()).isFalse()
    }

    @Test
    fun `preHandle sets tenant context and returns true when user and tenant are resolved`() {
        val userId = "user-123"
        val tenantId = UUID.randomUUID()
        every { request.getAttribute("auth_user_id") } returns userId
        every { tenantResolver.resolveTenantId(userId) } returns tenantId

        val result = interceptor.preHandle(request, response, handler)

        expectThat(result).isTrue()
        expectThat(TenantContext.get()).isEqualTo(tenantId)
        verify { request.setAttribute(TenantContext.REQUEST_ATTRIBUTE, tenantId) }
    }

    @Test
    fun `preHandle sends 403 and returns false when tenant resolver returns null`() {
        val userId = "user-no-org"
        every { request.getAttribute("auth_user_id") } returns userId
        every { tenantResolver.resolveTenantId(userId) } returns null

        val result = interceptor.preHandle(request, response, handler)

        expectThat(result).isFalse()
        verify { response.sendError(HttpServletResponse.SC_FORBIDDEN, any()) }
        expectThat(TenantContext.isPresent()).isFalse()
    }

    @Test
    fun `afterCompletion clears tenant context`() {
        TenantContext.set(UUID.randomUUID())

        interceptor.afterCompletion(request, response, handler, null)

        expectThat(TenantContext.isPresent()).isFalse()
    }

    @Test
    fun `afterCompletion is safe when tenant context was not set`() {
        interceptor.afterCompletion(request, response, handler, null)
        expectThat(TenantContext.isPresent()).isFalse()
    }
}
