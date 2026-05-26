package tech.sergiodelgado.saasstarter.autoconfigure

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
    val billing: Billing = Billing(),
    val security: Security = Security(),
    val web: Web = Web(),
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

    data class Billing(
        val enabled: Boolean = true,
        /** Stripe API key (`sk_*`). Set via STRIPE_API_KEY env var typically. */
        val apiKey: String = "",
        /** Stripe webhook signing secret (`whsec_*`). */
        val webhookSecret: String = "",
        /** URL to send the customer to after successful checkout. */
        val successUrl: String = "",
        /** URL to send the customer to if checkout is cancelled. */
        val cancelUrl: String = "",
        /** URL to send the customer to after closing the billing portal. */
        val portalReturnUrl: String = "",
        /**
         * Map of plan name (matching [BillingPlan.name]) to Stripe Price ID.
         * Apps populate this in application.yml; lookup is case-sensitive on
         * the plan name.
         */
        val planPrices: Map<String, String> = emptyMap(),
    )

    data class Security(
        val enabled: Boolean = true,
        /** Zitadel-style JWKS endpoint URL. Required when security.enabled=true. */
        val jwksUrl: String = "",
        /** Expected `iss` claim in JWT tokens. Required when security.enabled=true. */
        val issuer: String = "",
    )

    data class Web(
        val enabled: Boolean = true,
        val exceptionHandler: ExceptionHandler = ExceptionHandler(),
    ) {
        data class ExceptionHandler(val enabled: Boolean = true)
    }
}
