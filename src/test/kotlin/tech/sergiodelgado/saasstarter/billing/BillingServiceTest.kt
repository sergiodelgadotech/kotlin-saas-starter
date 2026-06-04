package tech.sergiodelgado.saasstarter.billing

import com.stripe.StripeClient
import com.stripe.model.Customer
import com.stripe.model.billingportal.Session as PortalSession
import com.stripe.model.checkout.Session as CheckoutSession
import com.stripe.param.CustomerCreateParams
import com.stripe.param.billingportal.SessionCreateParams as PortalSessionCreateParams
import com.stripe.param.checkout.SessionCreateParams as CheckoutSessionCreateParams
import com.stripe.service.BillingPortalService
import com.stripe.service.CheckoutService
import com.stripe.service.CustomerService
import com.stripe.service.billingportal.SessionService as PortalSessionService
import com.stripe.service.checkout.SessionService as CheckoutSessionService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs
import tech.sergiodelgado.saasstarter.autoconfigure.SaasStarterProperties
import tech.sergiodelgado.saasstarter.tenant.TenantContext
import tech.sergiodelgado.saasstarter.web.NotFoundException
import java.util.UUID

class BillingServiceTest {

    private val subscriptionRepository = mockk<SubscriptionRepository>()
    private val mockCustomerService = mockk<CustomerService>()
    private val mockCheckoutService = mockk<CheckoutService>()
    private val mockCheckoutSessionService = mockk<CheckoutSessionService>()
    private val mockPortalService = mockk<BillingPortalService>()
    private val mockPortalSessionService = mockk<PortalSessionService>()
    private val stripeClient = mockk<StripeClient>()
    private val properties = SaasStarterProperties(
        billing = SaasStarterProperties.Billing(
            apiKey = "sk_test_dummy",
            successUrl = "https://example.com/success",
            cancelUrl = "https://example.com/cancel",
            portalReturnUrl = "https://example.com/portal",
            planPrices = mapOf("PRO" to "price_pro_123"),
        )
    )
    private val service = BillingService(subscriptionRepository, properties, stripeClient)
    private val orgId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        TenantContext.set(orgId)
        every { stripeClient.customers() } returns mockCustomerService
        every { stripeClient.checkout() } returns mockCheckoutService
        every { mockCheckoutService.sessions() } returns mockCheckoutSessionService
        every { stripeClient.billingPortal() } returns mockPortalService
        every { mockPortalService.sessions() } returns mockPortalSessionService
    }

    @AfterEach
    fun tearDown() {
        TenantContext.clear()
    }

    // ── currentSubscription ───────────────────────────────────────────────────

    @Test
    fun `currentSubscription returns subscription when found`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_1")
        every { subscriptionRepository.findByOrganizationId(orgId) } returns sub

        expectThat(service.currentSubscription()).isEqualTo(sub)
    }

    @Test
    fun `currentSubscription returns null when no subscription exists`() {
        every { subscriptionRepository.findByOrganizationId(orgId) } returns null

        expectThat(service.currentSubscription()).isNull()
    }

    // ── createCheckoutSession ─────────────────────────────────────────────────

    @Test
    fun `createCheckoutSession passes customer, price, mode, and URLs to Stripe via StripeClient`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_abc")
        every { subscriptionRepository.findByOrganizationId(orgId) } returns sub
        val mockSession = mockk<CheckoutSession> { every { url } returns "https://checkout.stripe.com/pay/cs_test" }
        val slot = slot<CheckoutSessionCreateParams>()
        every { mockCheckoutSessionService.create(capture(slot)) } returns mockSession

        val result = service.createCheckoutSession(DefaultBillingPlan.PRO)

        expectThat(result).isEqualTo("https://checkout.stripe.com/pay/cs_test")
        val params = slot.captured
        expectThat(params.customer).isEqualTo("cus_abc")
        expectThat(params.mode).isEqualTo(CheckoutSessionCreateParams.Mode.SUBSCRIPTION)
        val lineItem = params.lineItems.single()
        expectThat(lineItem.price).isEqualTo("price_pro_123")
        expectThat(lineItem.quantity).isEqualTo(1L)
        expectThat(params.successUrl).isEqualTo("https://example.com/success")
        expectThat(params.cancelUrl).isEqualTo("https://example.com/cancel")
    }

    // ── createPortalSession ───────────────────────────────────────────────────

    @Test
    fun `createPortalSession passes customer and returnUrl to Stripe via StripeClient`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_abc")
        every { subscriptionRepository.findByOrganizationId(orgId) } returns sub
        val mockSession = mockk<PortalSession> { every { url } returns "https://billing.stripe.com/session/portal_test" }
        val slot = slot<PortalSessionCreateParams>()
        every { mockPortalSessionService.create(capture(slot)) } returns mockSession

        val result = service.createPortalSession()

        expectThat(result).isEqualTo("https://billing.stripe.com/session/portal_test")
        val params = slot.captured
        expectThat(params.customer).isEqualTo("cus_abc")
        expectThat(params.returnUrl).isEqualTo("https://example.com/portal")
    }

    // ── createCustomer ────────────────────────────────────────────────────────

    @Test
    fun `createCustomer returns Stripe customer ID and attaches organizationId metadata`() {
        val mockCustomer = mockk<Customer> { every { id } returns "cus_test123" }
        val slot = slot<CustomerCreateParams>()
        every { mockCustomerService.create(capture(slot)) } returns mockCustomer

        val result = service.createCustomer(
            organizationId = orgId,
            email = "alice@example.com",
            name = "Alice",
            metadata = mapOf("source" to "signup"),
        )

        expectThat(result).isEqualTo("cus_test123")
        val params = slot.captured
        expectThat(params.email).isEqualTo("alice@example.com")
        expectThat(params.name).isEqualTo("Alice")
        @Suppress("UNCHECKED_CAST")
        val meta = params.metadata as Map<String, String>
        expectThat(meta["source"]).isEqualTo("signup")
        expectThat(meta["organizationId"]).isEqualTo(orgId.toString())
    }

    @Test
    fun `createCustomer overrides caller-supplied organizationId metadata`() {
        val mockCustomer = mockk<Customer> { every { id } returns "cus_test123" }
        val slot = slot<CustomerCreateParams>()
        every { mockCustomerService.create(capture(slot)) } returns mockCustomer

        service.createCustomer(
            organizationId = orgId,
            email = "alice@example.com",
            metadata = mapOf("organizationId" to "WRONG"),
        )

        @Suppress("UNCHECKED_CAST")
        val meta = slot.captured.metadata as Map<String, String>
        expectThat(meta["organizationId"]).isEqualTo(orgId.toString())
    }

    @Test
    fun `createCustomer throws when Stripe API key is blank`() {
        val blankKeyService = BillingService(
            subscriptionRepository,
            SaasStarterProperties(),
            stripeClient,
        )

        val ex = assertThrows<IllegalStateException> {
            blankKeyService.createCustomer(orgId, "alice@example.com")
        }
        expectThat(ex.message!!).contains("Stripe API key not configured")
        verify(exactly = 0) { mockCustomerService.create(any<CustomerCreateParams>()) }
    }

    // ── ensureSubscription ────────────────────────────────────────────────────

    @Test
    fun `ensureSubscription inserts new TRIALING subscription when none exists`() {
        every { subscriptionRepository.findByOrganizationId(orgId) } returns null
        every { subscriptionRepository.save(any<Subscription>()) } answers { firstArg() }

        val result = service.ensureSubscription(orgId, "cus_test123")

        expectThat(result.organizationId).isEqualTo(orgId)
        expectThat(result.externalCustomerId).isEqualTo("cus_test123")
        expectThat(result.status).isEqualTo(SubscriptionStatus.TRIALING)
        expectThat(result.plan).isEqualTo("STARTER")
        verify(exactly = 1) { subscriptionRepository.save(any<Subscription>()) }
    }

    @Test
    fun `ensureSubscription is idempotent when subscription already exists for same customer`() {
        val existing = Subscription(
            organizationId = orgId,
            externalCustomerId = "cus_test123",
            status = SubscriptionStatus.ACTIVE,
        )
        every { subscriptionRepository.findByOrganizationId(orgId) } returns existing

        val result = service.ensureSubscription(orgId, "cus_test123")

        expectThat(result).isSameInstanceAs(existing)
        verify(exactly = 0) { subscriptionRepository.save(any<Subscription>()) }
    }

    @Test
    fun `ensureSubscription throws when existing subscription has a different customer ID`() {
        val existing = Subscription(organizationId = orgId, externalCustomerId = "cus_OLD")
        every { subscriptionRepository.findByOrganizationId(orgId) } returns existing

        val ex = assertThrows<IllegalStateException> {
            service.ensureSubscription(orgId, "cus_NEW")
        }
        expectThat(ex.message!!).contains(orgId.toString())
        expectThat(ex.message!!).contains("different customer ID")
    }

    @Test
    fun `ensureSubscription persists custom BillingPlan`() {
        every { subscriptionRepository.findByOrganizationId(orgId) } returns null
        val slot = slot<Subscription>()
        every { subscriptionRepository.save(capture(slot)) } answers { firstArg() }

        service.ensureSubscription(orgId, "cus_test123", DefaultBillingPlan.PRO)

        expectThat(slot.captured.plan).isEqualTo("PRO")
    }
}
