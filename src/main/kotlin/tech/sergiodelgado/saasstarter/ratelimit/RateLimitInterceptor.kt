package tech.sergiodelgado.saasstarter.ratelimit

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.http.HttpStatus
import org.springframework.util.AntPathMatcher
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

data class RouteConfig(
    val pathPattern: String,
    val limit: Int? = null,
    val window: Duration? = null,
)

class RateLimitInterceptor(
    private val rateLimiter: RateLimiter,
    private val defaultLimit: Int = 100,
    private val defaultWindow: Duration = Duration.ofMinutes(1),
    private val routes: List<RouteConfig> = emptyList(),
) : HandlerInterceptor {

    private val pathMatcher = AntPathMatcher()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val (limit, window) = resolve(request.requestURI, handler)
        val key = "rate:${request.requestURI}:${request.remoteAddr}"

        val allowed = rateLimiter.isAllowed(key, limit = limit, window = window)

        if (!allowed) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("Too many requests")
            return false
        }
        return true
    }

    private fun resolve(uri: String, handler: Any): Pair<Int, Duration> {
        if (handler is HandlerMethod) {
            val annotation = AnnotatedElementUtils.findMergedAnnotation(handler.method, RateLimit::class.java)
            if (annotation != null) {
                val window = try {
                    Duration.parse(annotation.window)
                } catch (ex: Exception) {
                    throw IllegalArgumentException(
                        "@RateLimit on ${handler.method.declaringClass.simpleName}.${handler.method.name} " +
                            "has invalid window '${annotation.window}': ${ex.message}",
                        ex,
                    )
                }
                return annotation.limit to window
            }
        }

        for (route in routes) {
            if (pathMatcher.match(route.pathPattern, uri)) {
                return (route.limit ?: defaultLimit) to (route.window ?: defaultWindow)
            }
        }

        return defaultLimit to defaultWindow
    }
}
