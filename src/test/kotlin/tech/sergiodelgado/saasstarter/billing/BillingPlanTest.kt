package tech.sergiodelgado.saasstarter.billing

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isA

class BillingPlanTest {

    @Test
    fun `default billing plan enum has the three canonical values`() {
        expectThat(DefaultBillingPlan.entries.toList())
            .containsExactly(DefaultBillingPlan.STARTER, DefaultBillingPlan.PRO, DefaultBillingPlan.ENTERPRISE)
    }

    @Test
    fun `default billing plan implements BillingPlan`() {
        expectThat(DefaultBillingPlan.STARTER as Any).isA<BillingPlan>()
    }
}
