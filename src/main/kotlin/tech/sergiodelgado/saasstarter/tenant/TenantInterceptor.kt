package tech.sergiodelgado.saasstarter.tenant

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import tech.sergiodelgado.saasstarter.security.JwtAuthFilter
import org.springframework.web.servlet.HandlerInterceptor

class TenantInterceptor(
    private val tenantResolver: TenantResolver
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val userId = JwtAuthFilter.getUserId(request) ?: return true

        val tenantId = tenantResolver.resolveTenantId(userId)
            ?: run {
                response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "User not associated with any tenant"
                )
                return false
            }

        TenantContext.set(tenantId)
        request.setAttribute(TenantContext.REQUEST_ATTRIBUTE, tenantId)
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        TenantContext.clear()
    }
}
