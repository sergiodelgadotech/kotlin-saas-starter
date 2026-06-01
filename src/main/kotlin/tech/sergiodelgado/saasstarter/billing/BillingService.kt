package tech.sergiodelgado.saasstarter.billing

import com.stripe.StripeClient
import com.stripe.param.CustomerCreateParams
import com.stripe.param.billingportal.SessionCreateParams as PortalSessionCreateParams
import com.stripe.param.checkout.SessionCreateParams as CheckoutSessionCreateParams
import tech.sergiodelgado.saasstarter.autoconfigure.SaasStarterProperties
import tech.sergiodelgado.saasstarter.tenant.TenantContext
import tech.sergiodelgado.saasstarter.web.NotFoundException
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
open class BillingService(
    private val subscriptionRepository: SubscriptionRepository,
    private val properties: SaasStarterProperties,
    private val stripeClient: StripeClient,
) {

    fun currentSubscription(): Subscription =
        subscriptionRepository.findByOrganizationId(TenantContext.get())
            ?: throw NotFoundException("No subscription found for organization")

    fun createCheckoutSession(plan: BillingPlan): String {
        val sub = currentSubscription()
        val priceId = priceIdFor(plan)
        return stripeClient.checkout().sessions().create(
            CheckoutSessionCreateParams.builder()
                .setCustomer(sub.externalCustomerId)
                .setMode(CheckoutSessionCreateParams.Mode.SUBSCRIPTION)
                .addLineItem(
                    CheckoutSessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1)
                        .build()
                )
                .setSuccessUrl(properties.billing.successUrl)
                .setCancelUrl(properties.billing.cancelUrl)
                .build()
        ).url
    }

    fun createPortalSession(): String {
        val sub = currentSubscription()
        return stripeClient.billingPortal().sessions().create(
            PortalSessionCreateParams.builder()
                .setCustomer(sub.externalCustomerId)
                .setReturnUrl(properties.billing.portalReturnUrl)
                .build()
        ).url
    }

    /**
     * Creates a Stripe customer for [organizationId] and returns the resulting `cus_*` ID.
     *
     * [organizationId] is attached to the Stripe customer's metadata under the key
     * `organizationId`, overriding any caller-supplied value for that key. This lets
     * webhooks resolve the originating organization from any Stripe object tied to the customer.
     *
     * @throws IllegalStateException if `saasstarter.billing.api-key` is not configured.
     */
    fun createCustomer(
        organizationId: UUID,
        email: String,
        name: String? = null,
        metadata: Map<String, String> = emptyMap(),
    ): String {
        check(properties.billing.apiKey.isNotBlank()) {
            "Stripe API key not configured; set saasstarter.billing.api-key"
        }
        val params = CustomerCreateParams.builder()
            .setEmail(email)
            .apply { name?.let { setName(it) } }
            .putAllMetadata(metadata + ("organizationId" to organizationId.toString()))
            .build()
        return stripeClient.customers().create(params).id
    }

    /**
     * Returns the existing [Subscription] for [organizationId], or persists a new one with
     * [SubscriptionStatus.TRIALING] status if none exists. Idempotent on a matching customer ID.
     *
     * @throws IllegalStateException if a subscription already exists for [organizationId]
     *   but is bound to a different [externalCustomerId][Subscription.externalCustomerId] than
     *   [customerId], which indicates a duplicate-customer bug.
     */
    fun ensureSubscription(
        organizationId: UUID,
        customerId: String,
        plan: BillingPlan = DefaultBillingPlan.STARTER,
    ): Subscription {
        subscriptionRepository.findByOrganizationId(organizationId)?.let { existing ->
            check(existing.externalCustomerId == customerId) {
                "Subscription for organization $organizationId already exists with a different customer ID"
            }
            return existing
        }
        return subscriptionRepository.save(
            Subscription(
                organizationId = organizationId,
                externalCustomerId = customerId,
                plan = plan.name,
                status = SubscriptionStatus.TRIALING,
            )
        )
    }

    private fun priceIdFor(plan: BillingPlan): String =
        properties.billing.planPrices[plan.name]
            ?: error("No Stripe price ID configured for plan ${plan.name}")
}
