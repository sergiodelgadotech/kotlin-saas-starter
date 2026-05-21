package org.granchi.saasstarter.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Root configuration properties for kotlin-saas-starter. Each nested group
 * maps to a focused @AutoConfiguration class: session, jobs, cache, tenant,
 * rate-limit, billing.
 */
@ConfigurationProperties(prefix = "saasstarter")
data class SaasStarterProperties(
    val enabled: Boolean = true,
    val session: Session = Session(),
    val jobs: Jobs = Jobs(),
    val cache: Cache = Cache(),
    val tenant: Tenant = Tenant(),
    val rateLimit: RateLimit = RateLimit(),
) {
    data class Session(
        val enabled: Boolean = true,
    )

    data class Jobs(
        val enabled: Boolean = true,
    )

    data class Cache(
        val enabled: Boolean = true,
        val defaultTtl: Duration = Duration.ofMinutes(10),
        val configurations: Map<String, CacheEntry> = emptyMap(),
    ) {
        data class CacheEntry(
            val ttl: Duration? = null,
        )
    }

    data class Tenant(
        val enabled: Boolean = true,
        val pathPatterns: List<String> = emptyList(),
        val excludePathPatterns: List<String> = emptyList(),
    )

    data class RateLimit(
        val enabled: Boolean = true,
        val pathPatterns: List<String> = emptyList(),
    )
}
