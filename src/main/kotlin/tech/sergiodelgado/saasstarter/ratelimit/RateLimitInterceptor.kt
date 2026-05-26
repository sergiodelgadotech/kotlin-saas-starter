package tech.sergiodelgado.saasstarter.ratelimit

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

class RateLimitInterceptor(private val rateLimiter: RateLimiter) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val ip = request.remoteAddr
        val key = "rate:${request.requestURI}:$ip"

        // Webhook endpoints: 100 requests per minute per IP
        val allowed = rateLimiter.isAllowed(key, limit = 100, window = Duration.ofMinutes(1))

        if (!allowed) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("Too many requests")
            return false
        }
        return true
    }
}
