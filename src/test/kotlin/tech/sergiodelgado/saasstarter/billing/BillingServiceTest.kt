package tech.sergiodelgado.saasstarter.billing

import com.stripe.StripeClient
import com.stripe.model.Customer
import com.stripe.param.CustomerCreateParams
import com.stripe.service.CustomerService
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
import strikt.assertions.isSameInstanceAs
import tech.sergiodelgado.saasstarter.autoconfigure.SaasStarterProperties
import tech.sergiodelgado.saasstarter.tenant.TenantContext
import tech.sergiodelgado.saasstarter.web.NotFoundException
import java.util.UUID

class BillingServiceTest {

    private val subscriptionRepository = mockk<SubscriptionRepository>()
    private val mockCustomerService = mockk<CustomerService>()
    private val stripeClient = mockk<StripeClient>()
    private val properties = SaasStarterProperties(
        billing = SaasStarterProperties.Billing(apiKey = "sk_test_dummy")
    )
    private val service = BillingService(subscriptionRepository, properties, stripeClient)
    private val orgId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        TenantContext.set(orgId)
        every { stripeClient.customers() } returns mockCustomerService
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
    fun `currentSubscription throws NotFoundException when no subscription exists`() {
        every { subscriptionRepository.findByOrganizationId(orgId) } returns null

        assertThrows<NotFoundException> { service.currentSubscription() }
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
