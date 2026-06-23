package tech.sergiodelgado.saasstarter.billing

import com.stripe.StripeClient
import com.stripe.param.CustomerCreateParams
import com.stripe.param.SubscriptionListParams
import com.stripe.param.billingportal.SessionCreateParams as PortalSessionCreateParams
import com.stripe.param.checkout.SessionCreateParams as CheckoutSessionCreateParams
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import tech.sergiodelgado.saasstarter.autoconfigure.SaasStarterProperties
import tech.sergiodelgado.saasstarter.organization.DefaultMemberRole
import tech.sergiodelgado.saasstarter.organization.MemberRepository
import tech.sergiodelgado.saasstarter.organization.OrganizationRepository
import tech.sergiodelgado.saasstarter.tenant.TenantContext
import tech.sergiodelgado.saasstarter.web.NotFoundException
import java.util.UUID

@Transactional
open class BillingService(
    private val subscriptionRepository: SubscriptionRepository,
    private val organizationRepository: OrganizationRepository,
    private val memberRepository: MemberRepository,
    private val properties: SaasStarterProperties,
    private val stripeClient: StripeClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val planByPriceId: Map<String, String> =
        properties.billing.planPrices.entries.associate { (plan, price) -> price to plan }

    fun currentSubscription(): Subscription? =
        subscriptionRepository.findByOrganizationId(TenantContext.get())

    /**
     * Fetches the current Stripe subscription for this tenant's customer and updates the local
     * [Subscription] row to match it. Call this on the success-return from Stripe Checkout to
     * ensure the plan is correct before the async webhook lands.
     *
     * Returns the (possibly updated) subscription, or null if no local subscription exists.
     */
    /**
     * Ensures the current tenant's subscription has a Stripe customer, creating one if needed.
     *
     * - If no subscription exists, creates a Stripe customer then a new STARTER subscription.
     * - If a subscription exists but has no Stripe customer (e.g. free STARTER plan upgrading),
     *   creates a Stripe customer and attaches it.
     * - If a subscription already has a Stripe customer, returns it unchanged.
     */
    fun ensureStripeCustomer(): Subscription {
        val organizationId = TenantContext.get()
        val sub = subscriptionRepository.findByOrganizationId(organizationId)
        if (sub?.externalCustomerId != null) return sub

        val org = checkNotNull(organizationRepository.findById(organizationId).orElse(null)) {
            "Organization $organizationId not found"
        }
        val ownerEmail = memberRepository.findByOrganizationId(organizationId)
            .firstOrNull { it.role == DefaultMemberRole.OWNER.name }?.email.orEmpty()
        val customerId = createCustomer(organizationId, email = ownerEmail, name = org.name)

        return if (sub == null) {
            ensureSubscription(organizationId, customerId)
        } else {
            attachStripeCustomer(customerId)
        }
    }

    /**
     * Attaches [customerId] to the current tenant's subscription, which must already exist
     * and must not already have a Stripe customer. Used when upgrading a free-plan subscription
     * to a paid plan that requires a Stripe customer.
     */
    fun attachStripeCustomer(customerId: String): Subscription {
        val organizationId = TenantContext.get()
        val sub = checkNotNull(subscriptionRepository.findByOrganizationId(organizationId)) {
            "No subscription found for organization $organizationId"
        }
        val updated = sub.copy(externalCustomerId = customerId).apply { _new = false }
        return subscriptionRepository.save(updated)
    }

    fun syncFromStripe(): Subscription? {
        val sub = currentSubscription() ?: return null
        val customerId = sub.externalCustomerId ?: return sub
        val stripeSub = stripeClient.v1().subscriptions()
            .list(
                SubscriptionListParams.builder()
                    .setCustomer(customerId)
                    .setStatus(SubscriptionListParams.Status.ALL)
                    .setLimit(1L)
                    .build()
            )
            .data
            .firstOrNull() ?: return sub   // no Stripe sub yet — return unchanged local row

        val updated = sub.copy(
            externalSubscriptionId = stripeSub.id,
            plan                   = StripeSubscriptionMapper.mapPlan(stripeSub, planByPriceId, log),
            status                 = StripeSubscriptionMapper.mapStatus(stripeSub.status),
            currentPeriodEnd       = StripeSubscriptionMapper.periodEnd(stripeSub),
            cancelAtPeriodEnd      = stripeSub.cancelAtPeriodEnd,
        )
        // copy() resets the @Transient _new flag to true (it's not a constructor
        // property), so without this the save() would INSERT and hit the PK. See
        // Subscription._new / OrganizationService for the same pattern.
        updated._new = false
        return subscriptionRepository.save(updated)
    }

    fun createCheckoutSession(plan: BillingPlan): String {
        val sub = currentSubscription() ?: throw NotFoundException("No subscription found for organization")
        val customerId = checkNotNull(sub.externalCustomerId) {
            "Subscription for organization ${sub.organizationId} has no Stripe customer; cannot create checkout session"
        }
        val priceId = priceIdFor(plan)
        return stripeClient.v1().checkout().sessions().create(
            CheckoutSessionCreateParams.builder()
                .setCustomer(customerId)
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
        val sub = currentSubscription() ?: throw NotFoundException("No subscription found for organization")
        val customerId = checkNotNull(sub.externalCustomerId) {
            "Subscription for organization ${sub.organizationId} has no Stripe customer; cannot create portal session"
        }
        return stripeClient.v1().billingPortal().sessions().create(
            PortalSessionCreateParams.builder()
                .setCustomer(customerId)
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
        return stripeClient.v1().customers().create(params).id
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
