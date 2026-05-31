package tech.sergiodelgado.saasstarter.billing

import com.stripe.model.Event
import com.stripe.model.Invoice
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
open class StripeWebhookHandler(
    private val subscriptionRepository: SubscriptionRepository,
    private val observationRegistry: ObservationRegistry = ObservationRegistry.NOOP,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handle(event: Event) {
        var outcome = "handled"
        val obs = Observation.createNotStarted("saasstarter.webhook.stripe", observationRegistry)
            .lowCardinalityKeyValue("event.type", event.type)
            .highCardinalityKeyValue("event.id", event.id ?: "unknown")
            .start()
        try {
            log.info("Processing Stripe event: ${event.type}")
            when (event.type) {
                "customer.subscription.created",
                "customer.subscription.updated"  -> handleSubscriptionUpdate(event)
                "customer.subscription.deleted"  -> handleSubscriptionCanceled(event)
                "invoice.payment_failed"         -> handlePaymentFailed(event)
                else -> { outcome = "skipped"; log.debug("Ignoring Stripe event: ${event.type}") }
            }
        } catch (e: Exception) {
            outcome = "error"
            obs.error(e)
            throw e
        } finally {
            obs.lowCardinalityKeyValue("outcome", outcome).stop()
        }
    }

    private fun handleSubscriptionUpdate(event: Event) {
        val stripeSub = event.dataObjectDeserializer
            .deserializeUnsafe() as com.stripe.model.Subscription

        val sub = subscriptionRepository.findByExternalCustomerId(stripeSub.customer) ?: run {
            log.warn("No subscription found for Stripe customer ${stripeSub.customer}")
            return
        }

        // In Stripe SDK v29+, currentPeriodEnd moved from Subscription to SubscriptionItem
        val periodEnd = stripeSub.items.data.firstOrNull()?.currentPeriodEnd
            ?.let { Instant.ofEpochSecond(it) }

        subscriptionRepository.save(
            sub.copy(
                externalSubscriptionId = stripeSub.id,
                plan                   = mapPlan(stripeSub),
                status                 = mapStatus(stripeSub.status),
                currentPeriodEnd       = periodEnd,
                cancelAtPeriodEnd      = stripeSub.cancelAtPeriodEnd,
            )
        )
    }

    private fun handleSubscriptionCanceled(event: Event) {
        val stripeSub = event.dataObjectDeserializer
            .deserializeUnsafe() as com.stripe.model.Subscription

        val sub = subscriptionRepository.findByExternalSubscriptionId(stripeSub.id) ?: return
        subscriptionRepository.save(sub.copy(status = SubscriptionStatus.CANCELED))
    }

    private fun handlePaymentFailed(event: Event) {
        val invoice = event.dataObjectDeserializer.deserializeUnsafe() as Invoice
        val sub = subscriptionRepository.findByExternalCustomerId(invoice.customer) ?: return
        subscriptionRepository.save(sub.copy(status = SubscriptionStatus.PAST_DUE))
        // TODO: send notification email via Resend
    }

    internal fun mapStatus(status: String) = when (status) {
        "active"   -> SubscriptionStatus.ACTIVE
        "trialing" -> SubscriptionStatus.TRIALING
        "past_due" -> SubscriptionStatus.PAST_DUE
        else       -> SubscriptionStatus.CANCELED
    }

    private fun mapPlan(stripeSub: com.stripe.model.Subscription): String {
        val priceId = stripeSub.items.data.firstOrNull()?.price?.id
            ?: return DefaultBillingPlan.STARTER.name
        // TODO: derive plan name by looking up the priceId in saasstarter.billing.plan-prices
        return when {
            priceId.contains("pro")        -> DefaultBillingPlan.PRO.name
            priceId.contains("enterprise") -> DefaultBillingPlan.ENTERPRISE.name
            else                           -> DefaultBillingPlan.STARTER.name
        }
    }
}
