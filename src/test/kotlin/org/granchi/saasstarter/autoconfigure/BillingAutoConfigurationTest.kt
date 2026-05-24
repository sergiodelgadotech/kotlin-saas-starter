package org.granchi.saasstarter.autoconfigure

import com.stripe.Stripe
import io.mockk.mockk
import org.granchi.saasstarter.billing.BillingService
import org.granchi.saasstarter.billing.StripeWebhookHandler
import org.granchi.saasstarter.billing.SubscriptionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class BillingAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(BillingAutoConfiguration::class.java))
        .withBean(SubscriptionRepository::class.java, { mockk<SubscriptionRepository>() })

    @BeforeEach
    fun resetStripeKey() {
        Stripe.apiKey = ""
    }

    @Test
    fun `BillingService and StripeWebhookHandler are registered`() {
        contextRunner.run { context ->
            expectThat(context.getBeansOfType(BillingService::class.java)).hasSize(1)
            expectThat(context.getBeansOfType(StripeWebhookHandler::class.java)).hasSize(1)
        }
    }

    @Test
    fun `Stripe apiKey static field is set from billing properties`() {
        contextRunner
            .withPropertyValues("saasstarter.billing.api-key=sk_test_runner_marker")
            .run {
                expectThat(Stripe.apiKey).isEqualTo("sk_test_runner_marker")
            }
    }

    @Test
    fun `autoconfig is skipped when billing enabled is false`() {
        contextRunner
            .withPropertyValues("saasstarter.billing.enabled=false")
            .run { context ->
                expectThat(context.getBeansOfType(BillingService::class.java)).hasSize(0)
            }
    }
}
