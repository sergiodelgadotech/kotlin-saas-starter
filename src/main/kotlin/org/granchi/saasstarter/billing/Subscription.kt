package org.granchi.saasstarter.billing

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("subscriptions")
data class Subscription(
    @Id val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    /** Customer ID at the billing provider (Stripe `cus_*`, Paddle, etc.). */
    val externalCustomerId: String,
    /** Subscription ID at the billing provider; null until the customer completes checkout. */
    val externalSubscriptionId: String? = null,
    val plan: String = DefaultBillingPlan.STARTER.name,
    val status: SubscriptionStatus = SubscriptionStatus.TRIALING,
    val currentPeriodEnd: Instant? = null,
    val cancelAtPeriodEnd: Boolean = false,
    val createdAt: Instant = Instant.now(),
) {
    fun isActive() = status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIALING
}
