package org.granchi.saasstarter.billing

/** Lifecycle states a subscription can be in. */
enum class SubscriptionStatus {
    TRIALING, ACTIVE, PAST_DUE, CANCELED
}
