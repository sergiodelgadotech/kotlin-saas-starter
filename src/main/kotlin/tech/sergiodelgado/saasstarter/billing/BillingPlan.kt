package tech.sergiodelgado.saasstarter.billing

/**
 * Marker interface for subscription plans. Apps replace [DefaultBillingPlan]
 * with their own enum (e.g. STARTER, PRO, ENTERPRISE, METERED) when needed.
 */
interface BillingPlan {
    val name: String
}

enum class DefaultBillingPlan : BillingPlan {
    STARTER, PRO, ENTERPRISE
}
