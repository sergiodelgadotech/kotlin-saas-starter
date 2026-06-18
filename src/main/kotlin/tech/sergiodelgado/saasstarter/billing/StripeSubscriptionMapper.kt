package tech.sergiodelgado.saasstarter.billing

import java.time.Instant

/**
 * Shared mapping logic for converting Stripe subscription objects to local model values.
 * Used by both [StripeWebhookHandler] and [BillingService.syncFromStripe].
 */
internal object StripeSubscriptionMapper {

    fun mapPlan(
        stripeSub: com.stripe.model.Subscription,
        planByPriceId: Map<String, String>,
        logger: org.slf4j.Logger,
    ): String {
        val priceId = stripeSub.items.data.firstOrNull()?.price?.id
            ?: return DefaultBillingPlan.STARTER.name
        return planByPriceId[priceId] ?: run {
            logger.warn(
                "Unknown Stripe priceId '{}' — not configured in saasstarter.billing.plan-prices; falling back to {}",
                priceId,
                DefaultBillingPlan.STARTER.name,
            )
            DefaultBillingPlan.STARTER.name
        }
    }

    fun mapStatus(status: String): SubscriptionStatus = when (status) {
        "active"   -> SubscriptionStatus.ACTIVE
        "trialing" -> SubscriptionStatus.TRIALING
        "past_due" -> SubscriptionStatus.PAST_DUE
        else       -> SubscriptionStatus.CANCELED
    }

    fun periodEnd(stripeSub: com.stripe.model.Subscription): Instant? =
        stripeSub.items.data.firstOrNull()?.currentPeriodEnd
            ?.let { Instant.ofEpochSecond(it) }
}
