package tech.sergiodelgado.saasstarter.billing

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.util.UUID

class SubscriptionTest {

    private val orgId: UUID = UUID.randomUUID()

    @Test
    fun `isActive returns true when status is ACTIVE`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_1", status = SubscriptionStatus.ACTIVE)
        expectThat(sub.isActive()).isTrue()
    }

    @Test
    fun `isActive returns true when status is TRIALING`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_1", status = SubscriptionStatus.TRIALING)
        expectThat(sub.isActive()).isTrue()
    }

    @Test
    fun `isActive returns false when status is CANCELED`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_1", status = SubscriptionStatus.CANCELED)
        expectThat(sub.isActive()).isFalse()
    }

    @Test
    fun `isActive returns false when status is PAST_DUE`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_1", status = SubscriptionStatus.PAST_DUE)
        expectThat(sub.isActive()).isFalse()
    }

    @Test
    fun `isNew returns true on a freshly constructed subscription`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_1")
        expectThat(sub.isNew()).isTrue()
    }

    @Test
    fun `isNew returns false after being set to false`() {
        val sub = Subscription(organizationId = orgId, externalCustomerId = "cus_1")
        sub._new = false
        expectThat(sub.isNew()).isFalse()
    }

    @Test
    fun `getId returns the subscription UUID`() {
        val id = UUID.randomUUID()
        val sub = Subscription(id = id, organizationId = orgId, externalCustomerId = "cus_1")
        expectThat(sub.getId()).isEqualTo(id)
    }
}
