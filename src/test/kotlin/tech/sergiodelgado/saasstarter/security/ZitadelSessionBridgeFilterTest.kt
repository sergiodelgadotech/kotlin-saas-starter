package tech.sergiodelgado.saasstarter.security

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import io.mockk.every
import io.mockk.mockk

class ZitadelSessionBridgeFilterTest {

    private val filter = ZitadelSessionBridgeFilter()
    private val request = MockHttpServletRequest()
    private val response = MockHttpServletResponse()
    private val chain = MockFilterChain()

    @AfterEach
    fun clearContext() = SecurityContextHolder.clearContext()

    @Test
    fun `sets auth_user_id from OidcUser subject when not already set`() {
        val oidcUser = mockk<OidcUser> { every { subject } returns "user-123" }
        SecurityContextHolder.getContext().authentication =
            OAuth2AuthenticationToken(oidcUser, emptyList(), "zitadel")

        filter.doFilter(request, response, chain)

        expectThat(request.getAttribute(JwtAuthFilter.USER_ID_ATTR)).isEqualTo("user-123")
    }

    @Test
    fun `does not overwrite auth_user_id already set by JwtAuthFilter`() {
        request.setAttribute(JwtAuthFilter.USER_ID_ATTR, "bearer-user")
        val oidcUser = mockk<OidcUser> { every { subject } returns "session-user" }
        SecurityContextHolder.getContext().authentication =
            OAuth2AuthenticationToken(oidcUser, emptyList(), "zitadel")

        filter.doFilter(request, response, chain)

        expectThat(request.getAttribute(JwtAuthFilter.USER_ID_ATTR)).isEqualTo("bearer-user")
    }

    @Test
    fun `does nothing when unauthenticated`() {
        filter.doFilter(request, response, chain)

        expectThat(request.getAttribute(JwtAuthFilter.USER_ID_ATTR)).isNull()
    }
}
