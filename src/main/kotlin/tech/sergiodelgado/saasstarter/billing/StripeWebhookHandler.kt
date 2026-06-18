package tech.sergiodelgado.saasstarter.billing

import com.stripe.model.Event
import com.stripe.model.Invoice
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import tech.sergiodelgado.saasstarter.autoconfigure.SaasStarterProperties
import tech.sergiodelgado.saasstarter.email.EmailMessage
import tech.sergiodelgado.saasstarter.email.EmailService

@Transactional
open class StripeWebhookHandler(
    private val subscriptionRepository: SubscriptionRepository,
    private val properties: SaasStarterProperties,
    private val observationRegistry: ObservationRegistry = ObservationRegistry.NOOP,
    private val emailService: EmailService? = null,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val planByPriceId: Map<String, String> =
        properties.billing.planPrices.entries.associate { (plan, price) -> price to plan }

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

        subscriptionRepository.save(
            sub.copy(
                externalSubscriptionId = stripeSub.id,
                plan                   = mapPlan(stripeSub),
                status                 = mapStatus(stripeSub.status),
                // In Stripe SDK v29+, currentPeriodEnd moved from Subscription to SubscriptionItem
                currentPeriodEnd       = StripeSubscriptionMapper.periodEnd(stripeSub),
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
        emailService?.let { svc ->
            composePaymentFailedEmail(invoice, sub)?.let { msg ->
                try {
                    svc.send(msg)
                } catch (e: Exception) {
                    log.warn("Payment-failed email to ${msg.to} could not be sent: ${e.message}")
                }
            }
        }
    }

    protected open fun composePaymentFailedEmail(invoice: Invoice, sub: Subscription): EmailMessage? {
        val to = invoice.customerEmail?.takeIf { it.isNotBlank() } ?: return null
        return EmailMessage(
            to = to,
            subject = "Action required: payment for your subscription failed",
            textBody = "Your most recent payment failed. Please update your payment method to avoid service interruption.",
        )
    }

    /** Delegates to [StripeSubscriptionMapper.mapStatus]; kept `internal` for test access. */
    internal fun mapStatus(status: String): SubscriptionStatus =
        StripeSubscriptionMapper.mapStatus(status)

    private fun mapPlan(stripeSub: com.stripe.model.Subscription): String =
        StripeSubscriptionMapper.mapPlan(stripeSub, planByPriceId, log)
}
