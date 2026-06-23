package tech.sergiodelgado.saasstarter.billing

import com.stripe.StripeClient
import com.stripe.model.Customer
import com.stripe.model.StripeCollection
import com.stripe.model.billingportal.Session as PortalSession
import com.stripe.model.checkout.Session as CheckoutSession
import com.stripe.param.CustomerCreateParams
import com.stripe.param.SubscriptionListParams
import com.stripe.param.billingportal.SessionCreateParams as PortalSessionCreateParams
import com.stripe.param.checkout.SessionCreateParams as CheckoutSessionCreateParams
import com.stripe.service.BillingPortalService
import com.stripe.service.CheckoutService
import com.stripe.service.CustomerService
import com.stripe.service.SubscriptionService
import com.stripe.service.V1Services
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
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs
import tech.sergiodelgado.saasstarter.autoconfigure.SaasStarterProperties
import tech.sergiodelgado.saasstarter.organization.DefaultMemberRole
import tech.sergiodelgado.saasstarter.organization.Member
import tech.sergiodelgado.saasstarter.organization.MemberRepository
import tech.sergiodelgado.saasstarter.organization.Organization
import tech.sergiodelgado.saasstarter.organization.OrganizationRepository
import tech.sergiodelgado.saasstarter.tenant.TenantContext
import tech.sergiodelgado.saasstarter.web.NotFoundException
import java.util.Optional
import java.util.UUID

class BillingServiceTest {

    private val subscriptionRepository = mockk<SubscriptionRepository>()
    private val organizationRepository = mockk<OrganizationRepository>()
    private val memberRepository = mockk<MemberRepository>()
    private val mockV1 = mockk<V1Services>()
    private val mockCustomerService = mockk<CustomerService>()
    private val mockCheckoutService = mockk<CheckoutService>()
    private val mockCheckoutSessionService = mockk<CheckoutSessionService>()
    private val mockPortalService = mockk<BillingPortalService>()
    private val mockPortalSessionService = mockk<PortalSessionService>()
    private val mockSubscriptionService = mockk<SubscriptionService>()
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
    private val service = BillingService(subscriptionRepository, organizationRepository, memberRepository, properties, stripeClient)
    private val orgId: UUID = UUID.randomUUID()
    private val testOrg = Organization(id = orgId, name = "Acme", slug = "acme-123456")
    private val ownerMember = Member(
        organizationId = orgId,
        externalUserId = "user-1",
        role = DefaultMemberRole.OWNER.name,
        email = "ceo@acme.com",
    )

    @BeforeEach
    fun setUp() {
        TenantContext.set(orgId)
        every { stripeClient.v1() } returns mockV1
        every { mockV1.customers() } returns mockCustomerService
        every { mockV1.checkout() } returns mockCheckoutService
        every { mockCheckoutService.sessions() } returns mockCheckoutSessionService
        every { mockV1.billingPortal() } returns mockPortalService
        every { mockPortalService.sessions() } returns mockPortalSessionService
        every { mockV1.subscriptions() } returns mockSubscriptionService
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
    fun `createCheckoutSession throws when subscription has no Stripe customer`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = null)
        every { subscriptionRepository.findByOrganizationId(orgId) } returns sub

        assertThrows<IllegalStateException> {
            service.createCheckoutSession(DefaultBillingPlan.PRO)
        }
        verify(exactly = 0) { mockCheckoutSessionService.create(any<CheckoutSessionCreateParams>()) }
    }

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
    fun `createPortalSession throws when subscription has no Stripe customer`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = null)
        every { subscriptionRepository.findByOrganizationId(orgId) } returns sub

        assertThrows<IllegalStateException> {
            service.createPortalSession()
        }
        verify(exactly = 0) { mockPortalSessionService.create(any<PortalSessionCreateParams>()) }
    }

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
            organizationRepository,
            memberRepository,
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

    // ── ensureStripeCustomer ──────────────────────────────────────────────────

    @Test
    fun `ensureStripeCustomer returns existing subscription unchanged when customer already set`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_existing")
        every { subscriptionRepository.findByOrganizationId(orgId) } returns sub

        val result = service.ensureStripeCustomer()

        expectThat(result).isSameInstanceAs(sub)
        verify(exactly = 0) { mockCustomerService.create(any<CustomerCreateParams>()) }
    }

    @Test
    fun `ensureStripeCustomer creates customer and attaches it when subscription has no customer`() {
        val sub = Subscription(organizationId = orgId).apply { _new = false }
        every { subscriptionRepository.findByOrganizationId(orgId) } returns sub
        every { organizationRepository.findById(orgId) } returns Optional.of(testOrg)
        every { memberRepository.findByOrganizationId(orgId) } returns listOf(ownerMember)
        val mockCustomer = mockk<Customer> { every { id } returns "cus_new" }
        every { mockCustomerService.create(any<CustomerCreateParams>()) } returns mockCustomer
        val saved = slot<Subscription>()
        every { subscriptionRepository.save(capture(saved)) } answers { firstArg() }

        service.ensureStripeCustomer()

        expectThat(saved.captured.externalCustomerId).isEqualTo("cus_new")
        expectThat(saved.captured.isNew()).isFalse()
    }

    @Test
    fun `ensureStripeCustomer creates customer and new subscription when none exists`() {
        every { subscriptionRepository.findByOrganizationId(orgId) } returns null
        every { organizationRepository.findById(orgId) } returns Optional.of(testOrg)
        every { memberRepository.findByOrganizationId(orgId) } returns listOf(ownerMember)
        val mockCustomer = mockk<Customer> { every { id } returns "cus_new" }
        every { mockCustomerService.create(any<CustomerCreateParams>()) } returns mockCustomer
        val saved = slot<Subscription>()
        every { subscriptionRepository.save(capture(saved)) } answers { firstArg() }

        service.ensureStripeCustomer()

        expectThat(saved.captured.externalCustomerId).isEqualTo("cus_new")
        expectThat(saved.captured.organizationId).isEqualTo(orgId)
    }

    // ── attachStripeCustomer ──────────────────────────────────────────────────

    @Test
    fun `attachStripeCustomer updates externalCustomerId and saves as not-new`() {
        val existing = Subscription(organizationId = orgId, plan = "STARTER").apply { _new = false }
        every { subscriptionRepository.findByOrganizationId(orgId) } returns existing
        val saved = slot<Subscription>()
        every { subscriptionRepository.save(capture(saved)) } answers { firstArg() }

        service.attachStripeCustomer("cus_upgraded")

        expectThat(saved.captured.externalCustomerId).isEqualTo("cus_upgraded")
        expectThat(saved.captured.isNew()).isFalse()
    }

    @Test
    fun `attachStripeCustomer throws when no subscription exists`() {
        every { subscriptionRepository.findByOrganizationId(orgId) } returns null

        assertThrows<IllegalStateException> { service.attachStripeCustomer("cus_x") }
        verify(exactly = 0) { subscriptionRepository.save(any()) }
    }

    // ── syncFromStripe ────────────────────────────────────────────────────────

    @Test
    fun `syncFromStripe returns sub unchanged and skips Stripe when externalCustomerId is null`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = null)
        every { subscriptionRepository.findByOrganizationId(orgId) } returns sub

        val result = service.syncFromStripe()

        expectThat(result).isSameInstanceAs(sub)
        verify(exactly = 0) { mockSubscriptionService.list(any<SubscriptionListParams>()) }
    }

    @Test
    fun `syncFromStripe returns null when no local subscription exists`() {
        every { subscriptionRepository.findByOrganizationId(orgId) } returns null

        expectThat(service.syncFromStripe()).isNull()
        verify(exactly = 0) { mockSubscriptionService.list(any<SubscriptionListParams>()) }
    }

    @Test
    fun `syncFromStripe returns unchanged local subscription when Stripe returns no subscriptions`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_abc")
        every { subscriptionRepository.findByOrganizationId(orgId) } returns sub
        val emptyCollection = mockk<StripeCollection<com.stripe.model.Subscription>> {
            every { data } returns mutableListOf()
        }
        every { mockSubscriptionService.list(any<SubscriptionListParams>()) } returns emptyCollection

        val result = service.syncFromStripe()

        expectThat(result).isSameInstanceAs(sub)
        verify(exactly = 0) { subscriptionRepository.save(any()) }
    }

    @Test
    fun `syncFromStripe updates local subscription plan and status from Stripe`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_abc", plan = "STARTER")
        every { subscriptionRepository.findByOrganizationId(orgId) } returns sub

        val price = mockk<com.stripe.model.Price> { every { id } returns "price_pro_123" }
        val item = mockk<com.stripe.model.SubscriptionItem> {
            every { this@mockk.price } returns price
            every { currentPeriodEnd } returns 1_700_000_000L
        }
        val items = mockk<com.stripe.model.SubscriptionItemCollection> {
            every { data } returns mutableListOf(item)
        }
        val stripeSub = mockk<com.stripe.model.Subscription> {
            every { this@mockk.items } returns items
            every { status } returns "active"
            every { id } returns "sub_pro_xyz"
            every { cancelAtPeriodEnd } returns false
            every { customer } returns "cus_abc"
        }
        val collection = mockk<StripeCollection<com.stripe.model.Subscription>> {
            every { data } returns mutableListOf(stripeSub)
        }
        every { mockSubscriptionService.list(any<SubscriptionListParams>()) } returns collection

        val slot = slot<Subscription>()
        every { subscriptionRepository.save(capture(slot)) } answers { firstArg() }

        service.syncFromStripe()

        verify { subscriptionRepository.save(any()) }
        expectThat(slot.captured.plan).isEqualTo(DefaultBillingPlan.PRO.name)
        expectThat(slot.captured.status).isEqualTo(SubscriptionStatus.ACTIVE)
        expectThat(slot.captured.externalSubscriptionId).isEqualTo("sub_pro_xyz")
    }

    @Test
    fun `syncFromStripe saves an existing subscription as not-new so Spring Data JDBC UPDATEs`() {
        // Regression: Subscription.copy() resets the @Transient _new flag to true (it is not a
        // constructor property), so before the fix syncFromStripe saved a "new" entity and Spring
        // Data JDBC issued an INSERT, hitting subscriptions_pkey on the success-return from Checkout.
        val existing = Subscription(organizationId = orgId, externalCustomerId = "cus_abc").apply { _new = false }
        every { subscriptionRepository.findByOrganizationId(orgId) } returns existing

        val price = mockk<com.stripe.model.Price> { every { id } returns "price_pro_123" }
        val item = mockk<com.stripe.model.SubscriptionItem> {
            every { this@mockk.price } returns price
            every { currentPeriodEnd } returns 1_700_000_000L
        }
        val items = mockk<com.stripe.model.SubscriptionItemCollection> { every { data } returns mutableListOf(item) }
        val stripeSub = mockk<com.stripe.model.Subscription> {
            every { this@mockk.items } returns items
            every { status } returns "active"
            every { id } returns "sub_pro_xyz"
            every { cancelAtPeriodEnd } returns false
            every { customer } returns "cus_abc"
        }
        val collection = mockk<StripeCollection<com.stripe.model.Subscription>> {
            every { data } returns mutableListOf(stripeSub)
        }
        every { mockSubscriptionService.list(any<SubscriptionListParams>()) } returns collection
        val slot = slot<Subscription>()
        every { subscriptionRepository.save(capture(slot)) } answers { firstArg() }

        service.syncFromStripe()

        expectThat(slot.captured.isNew()).isFalse()
    }

    @Test
    fun `syncFromStripe passes customer filter and limit=1 to Stripe list call`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_filter_test")
        every { subscriptionRepository.findByOrganizationId(orgId) } returns sub
        val emptyCollection = mockk<StripeCollection<com.stripe.model.Subscription>> {
            every { data } returns mutableListOf()
        }
        val paramsSlot = slot<SubscriptionListParams>()
        every { mockSubscriptionService.list(capture(paramsSlot)) } returns emptyCollection

        service.syncFromStripe()

        val params = paramsSlot.captured
        expectThat(params.customer).isEqualTo("cus_filter_test")
        expectThat(params.limit).isEqualTo(1L)
    }
}
