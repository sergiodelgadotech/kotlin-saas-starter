package tech.sergiodelgado.saasstarter.billing

import com.stripe.model.Event
import com.stripe.model.EventDataObjectDeserializer
import com.stripe.model.Invoice
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class StripeWebhookHandlerTest {

    private val subscriptionRepository = mockk<SubscriptionRepository>()
    private val handler = StripeWebhookHandler(subscriptionRepository)

    @Test
    fun `mapStatus maps Stripe status strings to SubscriptionStatus`() {
        expectThat(handler.mapStatus("active")).isEqualTo(SubscriptionStatus.ACTIVE)
        expectThat(handler.mapStatus("trialing")).isEqualTo(SubscriptionStatus.TRIALING)
        expectThat(handler.mapStatus("past_due")).isEqualTo(SubscriptionStatus.PAST_DUE)
        expectThat(handler.mapStatus("incomplete")).isEqualTo(SubscriptionStatus.CANCELED)
    }

    @Test
    fun `handle ignores unknown event types without throwing`() {
        val event = mockk<Event>()
        every { event.type } returns "some.unknown.event"
        handler.handle(event)
    }

    @Test
    fun `handle subscription deleted returns early when subscription not found`() {
        val stripeSub = mockk<com.stripe.model.Subscription>()
        val deserializer = mockk<EventDataObjectDeserializer>()
        val event = mockk<Event>()
        every { event.type } returns "customer.subscription.deleted"
        every { event.dataObjectDeserializer } returns deserializer
        every { deserializer.deserializeUnsafe() } returns stripeSub
        every { stripeSub.id } returns "sub_missing"
        every { subscriptionRepository.findByExternalSubscriptionId("sub_missing") } returns null

        handler.handle(event)
    }

    @Test
    fun `handle payment failed returns early when customer not found`() {
        val invoice = mockk<Invoice>()
        val deserializer = mockk<EventDataObjectDeserializer>()
        val event = mockk<Event>()
        every { event.type } returns "invoice.payment_failed"
        every { event.dataObjectDeserializer } returns deserializer
        every { deserializer.deserializeUnsafe() } returns invoice
        every { invoice.customer } returns "cus_missing"
        every { subscriptionRepository.findByExternalCustomerId("cus_missing") } returns null

        handler.handle(event)
    }

    @Test
    fun `handle subscription updated returns early when customer not found`() {
        val stripeSub = mockk<com.stripe.model.Subscription>()
        val deserializer = mockk<EventDataObjectDeserializer>()
        val event = mockk<Event>()
        every { event.type } returns "customer.subscription.updated"
        every { event.dataObjectDeserializer } returns deserializer
        every { deserializer.deserializeUnsafe() } returns stripeSub
        every { stripeSub.customer } returns "cus_unknown"
        every { subscriptionRepository.findByExternalCustomerId("cus_unknown") } returns null

        handler.handle(event)
    }
}
