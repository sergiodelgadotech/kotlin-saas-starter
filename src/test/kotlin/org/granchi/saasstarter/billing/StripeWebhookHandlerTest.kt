package org.granchi.saasstarter.billing

import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class StripeWebhookHandlerTest {

    @Test
    fun `mapStatus maps Stripe status strings to SubscriptionStatus`() {
        val handler = StripeWebhookHandler(mockk())
        expectThat(handler.mapStatus("active")).isEqualTo(SubscriptionStatus.ACTIVE)
        expectThat(handler.mapStatus("trialing")).isEqualTo(SubscriptionStatus.TRIALING)
        expectThat(handler.mapStatus("past_due")).isEqualTo(SubscriptionStatus.PAST_DUE)
        expectThat(handler.mapStatus("incomplete")).isEqualTo(SubscriptionStatus.CANCELED)
    }
}
