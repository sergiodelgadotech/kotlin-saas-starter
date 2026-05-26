package tech.sergiodelgado.saasstarter.billing

import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import tech.sergiodelgado.saasstarter.autoconfigure.SaasStarterProperties
import tech.sergiodelgado.saasstarter.tenant.TenantContext
import tech.sergiodelgado.saasstarter.web.NotFoundException
import org.springframework.transaction.annotation.Transactional

@Transactional
open class BillingService(
    private val subscriptionRepository: SubscriptionRepository,
    private val properties: SaasStarterProperties,
) {

    fun currentSubscription(): Subscription =
        subscriptionRepository.findByOrganizationId(TenantContext.get())
            ?: throw NotFoundException("No subscription found for organization")

    fun createCheckoutSession(plan: BillingPlan): String {
        val sub = currentSubscription()
        val priceId = priceIdFor(plan)
        val session = Session.create(
            SessionCreateParams.builder()
                .setCustomer(sub.externalCustomerId)
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1)
                        .build()
                )
                .setSuccessUrl(properties.billing.successUrl)
                .setCancelUrl(properties.billing.cancelUrl)
                .build()
        )
        return session.url
    }

    fun createPortalSession(): String {
        val sub = currentSubscription()
        return com.stripe.model.billingportal.Session.create(
            com.stripe.param.billingportal.SessionCreateParams.builder()
                .setCustomer(sub.externalCustomerId)
                .setReturnUrl(properties.billing.portalReturnUrl)
                .build()
        ).url
    }

    private fun priceIdFor(plan: BillingPlan): String =
        properties.billing.planPrices[plan.name]
            ?: error("No Stripe price ID configured for plan ${plan.name}")
}
