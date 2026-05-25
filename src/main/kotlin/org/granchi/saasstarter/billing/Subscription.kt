package org.granchi.saasstarter.billing

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * Spring Data JDBC considers an entity "new" (INSERT) only when the @Id field is null.
 * Since we pre-generate UUIDs, we implement Persistable<UUID> and carry a @Transient
 * _new flag so that freshly constructed entities are always inserted.
 *
 * The Kotlin property `val id` already satisfies `Persistable.getId()` via its
 * generated JVM getter, so we only need to declare `isNew()`.
 *
 * See [org.granchi.saasstarter.organization.Organization] for the same pattern.
 */
@Table("subscriptions")
data class Subscription(
    @Id @get:JvmName("entityId") val id: UUID = UUID.randomUUID(),
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
) : Persistable<UUID> {

    /** Tracks whether this instance has been persisted. Starts as true (INSERT on first save).
     * [AfterConvertCallback] in [BillingAutoConfiguration] flips this to false after every DB load. */
    @Transient
    @JvmField
    internal var _new: Boolean = true

    override fun getId(): UUID = id
    override fun isNew(): Boolean = _new

    fun isActive() = status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIALING
}
