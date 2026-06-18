package tech.sergiodelgado.saasstarter.billing

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class StripeSubscriptionMapperTest {

    private val planByPriceId = mapOf(
        "price_pro_monthly"      to DefaultBillingPlan.PRO.name,
        "price_enterprise_yearly" to DefaultBillingPlan.ENTERPRISE.name,
    )
    private val log = LoggerFactory.getLogger(StripeSubscriptionMapper::class.java)

    // ── mapPlan ───────────────────────────────────────────────────────────────

    @Test
    fun `mapPlan resolves PRO priceId to PRO`() {
        val stripeSub = stripeSubWithPriceId("price_pro_monthly", "active")

        expectThat(StripeSubscriptionMapper.mapPlan(stripeSub, planByPriceId, log))
            .isEqualTo(DefaultBillingPlan.PRO.name)
    }

    @Test
    fun `mapPlan resolves ENTERPRISE priceId to ENTERPRISE`() {
        val stripeSub = stripeSubWithPriceId("price_enterprise_yearly", "active")

        expectThat(StripeSubscriptionMapper.mapPlan(stripeSub, planByPriceId, log))
            .isEqualTo(DefaultBillingPlan.ENTERPRISE.name)
    }

    @Test
    fun `mapPlan falls back to STARTER silently when item list is empty`() {
        val stripeSub = stripeSubWithNoItems("trialing")

        val listAppender = captureLogsFor(StripeSubscriptionMapper::class.java)
        try {
            val result = StripeSubscriptionMapper.mapPlan(stripeSub, planByPriceId, log)
            expectThat(result).isEqualTo(DefaultBillingPlan.STARTER.name)
            expectThat(listAppender.list.filter { it.level == Level.WARN }).hasSize(0)
        } finally {
            detachAppender(StripeSubscriptionMapper::class.java, listAppender)
        }
    }

    @Test
    fun `mapPlan falls back to STARTER and warns for unknown priceId`() {
        val stripeSub = stripeSubWithPriceId("price_unknown_addon", "active")

        val listAppender = captureLogsFor(StripeSubscriptionMapper::class.java)
        try {
            val result = StripeSubscriptionMapper.mapPlan(stripeSub, planByPriceId, log)
            expectThat(result).isEqualTo(DefaultBillingPlan.STARTER.name)
            val warns = listAppender.list.filter { it.level == Level.WARN }
            expectThat(warns).hasSize(1)
            expectThat(warns[0].formattedMessage).isEqualTo(
                "Unknown Stripe priceId 'price_unknown_addon' — not configured in saasstarter.billing.plan-prices; falling back to STARTER"
            )
        } finally {
            detachAppender(StripeSubscriptionMapper::class.java, listAppender)
        }
    }

    // ── mapStatus ─────────────────────────────────────────────────────────────

    @Test
    fun `mapStatus maps all Stripe status strings correctly`() {
        expectThat(StripeSubscriptionMapper.mapStatus("active")).isEqualTo(SubscriptionStatus.ACTIVE)
        expectThat(StripeSubscriptionMapper.mapStatus("trialing")).isEqualTo(SubscriptionStatus.TRIALING)
        expectThat(StripeSubscriptionMapper.mapStatus("past_due")).isEqualTo(SubscriptionStatus.PAST_DUE)
        expectThat(StripeSubscriptionMapper.mapStatus("canceled")).isEqualTo(SubscriptionStatus.CANCELED)
        expectThat(StripeSubscriptionMapper.mapStatus("incomplete")).isEqualTo(SubscriptionStatus.CANCELED)
    }

    // ── periodEnd ─────────────────────────────────────────────────────────────

    @Test
    fun `periodEnd returns Instant from first item currentPeriodEnd`() {
        val stripeSub = stripeSubWithPriceId("price_pro_monthly", "active", periodEnd = 1_700_000_000L)

        val result = StripeSubscriptionMapper.periodEnd(stripeSub)
        expectThat(result?.epochSecond).isEqualTo(1_700_000_000L)
    }

    @Test
    fun `periodEnd returns null when item list is empty`() {
        val stripeSub = stripeSubWithNoItems("trialing")

        expectThat(StripeSubscriptionMapper.periodEnd(stripeSub)).isNull()
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun stripeSubWithPriceId(
        priceId: String,
        status: String,
        periodEnd: Long = 1_700_000_000L,
    ): com.stripe.model.Subscription {
        val price = mockk<com.stripe.model.Price> { every { id } returns priceId }
        val item = mockk<com.stripe.model.SubscriptionItem> {
            every { this@mockk.price } returns price
            every { currentPeriodEnd } returns periodEnd
        }
        val items = mockk<com.stripe.model.SubscriptionItemCollection> {
            every { data } returns mutableListOf(item)
        }
        return mockk {
            every { this@mockk.items } returns items
            every { this@mockk.status } returns status
            every { id } returns "sub_test"
            every { cancelAtPeriodEnd } returns false
            every { customer } returns "cus_test"
        }
    }

    private fun stripeSubWithNoItems(status: String): com.stripe.model.Subscription {
        val items = mockk<com.stripe.model.SubscriptionItemCollection> {
            every { data } returns mutableListOf()
        }
        return mockk {
            every { this@mockk.items } returns items
            every { this@mockk.status } returns status
            every { id } returns "sub_test"
            every { cancelAtPeriodEnd } returns false
            every { customer } returns "cus_test"
        }
    }

    private fun captureLogsFor(clazz: Class<*>): ListAppender<ILoggingEvent> {
        val appender = ListAppender<ILoggingEvent>().also { it.start() }
        (LoggerFactory.getLogger(clazz) as Logger).addAppender(appender)
        return appender
    }

    private fun detachAppender(clazz: Class<*>, appender: ListAppender<ILoggingEvent>) {
        (LoggerFactory.getLogger(clazz) as Logger).detachAppender(appender)
    }
}
