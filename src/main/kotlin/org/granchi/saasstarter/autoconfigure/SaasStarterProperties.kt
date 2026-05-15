package org.granchi.saasstarter.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

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
) {
    data class Session(
        val enabled: Boolean = true,
    )

    data class Jobs(
        val enabled: Boolean = true,
    )
}
