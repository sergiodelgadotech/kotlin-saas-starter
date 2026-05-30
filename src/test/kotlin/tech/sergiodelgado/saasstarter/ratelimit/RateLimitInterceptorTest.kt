package tech.sergiodelgado.saasstarter.ratelimit

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class RateLimitInterceptorTest {

    private val rateLimiter = mockk<RateLimiter>()
    private val interceptor = RateLimitInterceptor(rateLimiter)
    private val request = mockk<HttpServletRequest>()
    private val response = mockk<HttpServletResponse>(relaxed = true)
    private val handler = Any()

    @Test
    fun `allowed request returns true and does not write to response`() {
        every { request.remoteAddr } returns "10.0.0.1"
        every { request.requestURI } returns "/webhooks/stripe"
        every { rateLimiter.isAllowed(any(), any(), any()) } returns true

        val result = interceptor.preHandle(request, response, handler)

        expectThat(result).isTrue()
        verify(exactly = 0) { response.status = any() }
    }

    @Test
    fun `rate-limited request returns false and sets 429 status`() {
        every { request.remoteAddr } returns "10.0.0.1"
        every { request.requestURI } returns "/webhooks/stripe"
        every { rateLimiter.isAllowed(any(), any(), any()) } returns false

        val result = interceptor.preHandle(request, response, handler)

        expectThat(result).isFalse()
        verify { response.status = HttpStatus.TOO_MANY_REQUESTS.value() }
    }

    @Test
    fun `rate limit key is built from request uri and remote addr`() {
        val remoteAddr = "1.2.3.4"
        val requestUri = "/webhooks/test"
        every { request.remoteAddr } returns remoteAddr
        every { request.requestURI } returns requestUri
        every { rateLimiter.isAllowed(any(), any(), any()) } returns true

        interceptor.preHandle(request, response, handler)

        verify { rateLimiter.isAllowed("rate:$requestUri:$remoteAddr", any(), any()) }
    }
}
