package tech.sergiodelgado.saasstarter.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.filter.OncePerRequestFilter

class ZitadelSessionBridgeFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (request.getAttribute(USER_ID_ATTR) == null) {
            val auth = SecurityContextHolder.getContext().authentication
            if (auth is OAuth2AuthenticationToken) {
                val principal = auth.principal
                if (principal is OidcUser) {
                    request.setAttribute(USER_ID_ATTR, principal.subject)
                }
            }
        }
        chain.doFilter(request, response)
    }

    companion object {
        private const val USER_ID_ATTR = "auth_user_id"
    }
}
