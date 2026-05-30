package tech.sergiodelgado.saasstarter.billing

import com.stripe.model.Event
import com.stripe.model.EventDataObjectDeserializer
import com.stripe.model.Invoice
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.UUID

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

    @Test
    fun `handle subscription created saves updated subscription`() {
        val stripeSub = mockk<com.stripe.model.Subscription>()
        val deserializer = mockk<EventDataObjectDeserializer>()
        val event = mockk<Event>()
        val item = mockk<com.stripe.model.SubscriptionItem>()
        val itemCollection = mockk<com.stripe.model.SubscriptionItemCollection>()
        val price = mockk<com.stripe.model.Price>()
        val sub = Subscription(organizationId = UUID.randomUUID(), externalCustomerId = "cus_pro")

        every { event.type } returns "customer.subscription.created"
        every { event.dataObjectDeserializer } returns deserializer
        every { deserializer.deserializeUnsafe() } returns stripeSub
        every { stripeSub.customer } returns "cus_pro"
        every { subscriptionRepository.findByExternalCustomerId("cus_pro") } returns sub
        every { stripeSub.items } returns itemCollection
        every { itemCollection.data } returns mutableListOf(item)
        every { item.currentPeriodEnd } returns 1_700_000_000L
        every { item.price } returns price
        every { price.id } returns "price_pro_monthly"
        every { stripeSub.id } returns "sub_pro_123"
        every { stripeSub.status } returns "active"
        every { stripeSub.cancelAtPeriodEnd } returns false
        every { subscriptionRepository.save(any()) } returns mockk()

        handler.handle(event)

        verify { subscriptionRepository.save(match { it.plan == DefaultBillingPlan.PRO.name && it.status == SubscriptionStatus.ACTIVE }) }
    }

    @Test
    fun `handle subscription created defaults to STARTER when item list is empty`() {
        val stripeSub = mockk<com.stripe.model.Subscription>()
        val deserializer = mockk<EventDataObjectDeserializer>()
        val event = mockk<Event>()
        val itemCollection = mockk<com.stripe.model.SubscriptionItemCollection>()
        val sub = Subscription(organizationId = UUID.randomUUID(), externalCustomerId = "cus_new")

        every { event.type } returns "customer.subscription.created"
        every { event.dataObjectDeserializer } returns deserializer
        every { deserializer.deserializeUnsafe() } returns stripeSub
        every { stripeSub.customer } returns "cus_new"
        every { subscriptionRepository.findByExternalCustomerId("cus_new") } returns sub
        every { stripeSub.items } returns itemCollection
        every { itemCollection.data } returns mutableListOf()
        every { stripeSub.id } returns "sub_new_123"
        every { stripeSub.status } returns "trialing"
        every { stripeSub.cancelAtPeriodEnd } returns false
        every { subscriptionRepository.save(any()) } returns mockk()

        handler.handle(event)

        verify { subscriptionRepository.save(match { it.plan == DefaultBillingPlan.STARTER.name }) }
    }

    @Test
    fun `handle subscription deleted saves CANCELED status`() {
        val stripeSub = mockk<com.stripe.model.Subscription>()
        val deserializer = mockk<EventDataObjectDeserializer>()
        val event = mockk<Event>()
        val sub = Subscription(organizationId = UUID.randomUUID(), externalCustomerId = "cus_1", externalSubscriptionId = "sub_to_cancel")

        every { event.type } returns "customer.subscription.deleted"
        every { event.dataObjectDeserializer } returns deserializer
        every { deserializer.deserializeUnsafe() } returns stripeSub
        every { stripeSub.id } returns "sub_to_cancel"
        every { subscriptionRepository.findByExternalSubscriptionId("sub_to_cancel") } returns sub
        every { subscriptionRepository.save(any()) } returns mockk()

        handler.handle(event)

        verify { subscriptionRepository.save(match { it.status == SubscriptionStatus.CANCELED }) }
    }

    @Test
    fun `handle payment failed saves PAST_DUE status`() {
        val invoice = mockk<Invoice>()
        val deserializer = mockk<EventDataObjectDeserializer>()
        val event = mockk<Event>()
        val sub = Subscription(organizationId = UUID.randomUUID(), externalCustomerId = "cus_late")

        every { event.type } returns "invoice.payment_failed"
        every { event.dataObjectDeserializer } returns deserializer
        every { deserializer.deserializeUnsafe() } returns invoice
        every { invoice.customer } returns "cus_late"
        every { subscriptionRepository.findByExternalCustomerId("cus_late") } returns sub
        every { subscriptionRepository.save(any()) } returns mockk()

        handler.handle(event)

        verify { subscriptionRepository.save(match { it.status == SubscriptionStatus.PAST_DUE }) }
    }
}
