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
        chain: FilterChain
    ) {
        if (request.getAttribute(JwtAuthFilter.USER_ID_ATTR) == null) {
            (SecurityContextHolder.getContext().authentication as? OAuth2AuthenticationToken)
                ?.principal
                ?.let { it as? OidcUser }
                ?.subject
                ?.let { request.setAttribute(JwtAuthFilter.USER_ID_ATTR, it) }
        }
        chain.doFilter(request, response)
    }
}
