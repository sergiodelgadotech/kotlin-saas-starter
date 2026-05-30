package tech.sergiodelgado.saasstarter.ratelimit

/**
 * Overrides property-driven rate-limit values for a specific handler method.
 * Takes precedence over saasstarter.rate-limit.routes and the default.
 *
 * [window] must be an ISO-8601 duration string (e.g. "PT1M", "PT30S").
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RateLimit(val limit: Int, val window: String)
