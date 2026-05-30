package tech.sergiodelgado.saasstarter.ratelimit

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.method.HandlerMethod
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.time.Duration

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

    @Test
    fun `when no routes match, default limit and window are used`() {
        val customInterceptor = RateLimitInterceptor(
            rateLimiter = rateLimiter,
            defaultLimit = 50,
            defaultWindow = Duration.ofSeconds(30),
        )
        every { request.remoteAddr } returns "10.0.0.1"
        every { request.requestURI } returns "/foo"
        every { rateLimiter.isAllowed(any(), any(), any()) } returns true

        customInterceptor.preHandle(request, response, handler)

        verify { rateLimiter.isAllowed(any(), limit = 50, window = Duration.ofSeconds(30)) }
    }

    @Test
    fun `when route matches, uses route limit and window`() {
        val route = RouteConfig("/api/**", limit = 10, window = Duration.ofSeconds(30))
        val customInterceptor = RateLimitInterceptor(
            rateLimiter = rateLimiter,
            defaultLimit = 100,
            defaultWindow = Duration.ofMinutes(1),
            routes = listOf(route),
        )
        every { request.remoteAddr } returns "10.0.0.1"
        every { request.requestURI } returns "/api/webhooks/x"
        every { rateLimiter.isAllowed(any(), any(), any()) } returns true

        customInterceptor.preHandle(request, response, handler)

        verify { rateLimiter.isAllowed(any(), limit = 10, window = Duration.ofSeconds(30)) }
    }

    @Test
    fun `route with null limit inherits default limit and route with null window inherits default window`() {
        val route = RouteConfig("/api/**", limit = null, window = null)
        val customInterceptor = RateLimitInterceptor(
            rateLimiter = rateLimiter,
            defaultLimit = 75,
            defaultWindow = Duration.ofSeconds(45),
            routes = listOf(route),
        )
        every { request.remoteAddr } returns "10.0.0.1"
        every { request.requestURI } returns "/api/foo"
        every { rateLimiter.isAllowed(any(), any(), any()) } returns true

        customInterceptor.preHandle(request, response, handler)

        verify { rateLimiter.isAllowed(any(), limit = 75, window = Duration.ofSeconds(45)) }
    }

    @Test
    fun `first matching route wins when multiple routes overlap`() {
        val routes = listOf(
            RouteConfig("/api/webhooks/**", limit = 5, window = Duration.ofSeconds(10)),
            RouteConfig("/api/**", limit = 20, window = Duration.ofSeconds(60)),
        )
        val customInterceptor = RateLimitInterceptor(
            rateLimiter = rateLimiter,
            defaultLimit = 100,
            defaultWindow = Duration.ofMinutes(1),
            routes = routes,
        )
        every { request.remoteAddr } returns "10.0.0.1"
        every { request.requestURI } returns "/api/webhooks/stripe"
        every { rateLimiter.isAllowed(any(), any(), any()) } returns true

        customInterceptor.preHandle(request, response, handler)

        verify { rateLimiter.isAllowed(any(), limit = 5, window = Duration.ofSeconds(10)) }
    }

    @Test
    fun `@RateLimit annotation on handler method takes precedence over routes`() {
        val route = RouteConfig("/api/**", limit = 10, window = Duration.ofSeconds(30))
        val customInterceptor = RateLimitInterceptor(
            rateLimiter = rateLimiter,
            defaultLimit = 100,
            defaultWindow = Duration.ofMinutes(1),
            routes = listOf(route),
        )
        every { request.remoteAddr } returns "10.0.0.1"
        every { request.requestURI } returns "/api/webhooks/x"
        every { rateLimiter.isAllowed(any(), any(), any()) } returns true

        val handlerMethod = HandlerMethod(AnnotatedController(), AnnotatedController::class.java.getMethod("handle"))

        customInterceptor.preHandle(request, response, handlerMethod)

        verify { rateLimiter.isAllowed(any(), limit = 5, window = Duration.ofSeconds(10)) }
    }

    @Test
    fun `non-HandlerMethod handler falls through to routes and defaults without crashing`() {
        val customInterceptor = RateLimitInterceptor(
            rateLimiter = rateLimiter,
            defaultLimit = 42,
            defaultWindow = Duration.ofSeconds(60),
        )
        every { request.remoteAddr } returns "10.0.0.1"
        every { request.requestURI } returns "/foo"
        every { rateLimiter.isAllowed(any(), any(), any()) } returns true

        customInterceptor.preHandle(request, response, handler)

        verify { rateLimiter.isAllowed(any(), limit = 42, window = Duration.ofSeconds(60)) }
    }

    @Test
    fun `@RateLimit annotation with malformed window throws with method name in message`() {
        val customInterceptor = RateLimitInterceptor(rateLimiter = rateLimiter)
        every { request.remoteAddr } returns "10.0.0.1"
        every { request.requestURI } returns "/foo"

        val handlerMethod = HandlerMethod(BadAnnotatedController(), BadAnnotatedController::class.java.getMethod("handle"))

        val ex = assertThrows<Exception> {
            customInterceptor.preHandle(request, response, handlerMethod)
        }
        expectThat(ex.message!!).contains("BadAnnotatedController")
    }

    private class AnnotatedController {
        @RateLimit(limit = 5, window = "PT10S")
        fun handle() {}
    }

    private class BadAnnotatedController {
        @RateLimit(limit = 5, window = "NOT_ISO")
        fun handle() {}
    }
}
