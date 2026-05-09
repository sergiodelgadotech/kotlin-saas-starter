package org.granchi.saasstarter.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Root configuration properties for kotlin-saas-starter. Subsequent plans
 * extend this with nested groups (session, jobs, cache, tenant, rate-limit, billing).
 */
@ConfigurationProperties(prefix = "saasstarter")
data class SaasStarterProperties(
    val enabled: Boolean = true,
)
