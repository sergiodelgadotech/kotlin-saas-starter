package tech.sergiodelgado.saasstarter.billing

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import tech.sergiodelgado.saasstarter.autoconfigure.SaasStarterProperties
import tech.sergiodelgado.saasstarter.tenant.TenantContext
import tech.sergiodelgado.saasstarter.web.NotFoundException
import java.util.UUID

class BillingServiceTest {

    private val subscriptionRepository = mockk<SubscriptionRepository>()
    private val properties = SaasStarterProperties()
    private val service = BillingService(subscriptionRepository, properties)
    private val orgId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        TenantContext.set(orgId)
    }

    @AfterEach
    fun tearDown() {
        TenantContext.clear()
    }

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
}
