package org.granchi.saasstarter.autoconfigure

import com.stripe.Stripe
import org.granchi.saasstarter.billing.BillingService
import org.granchi.saasstarter.billing.Subscription
import org.granchi.saasstarter.billing.StripeWebhookHandler
import org.granchi.saasstarter.billing.SubscriptionRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.data.relational.core.mapping.event.AfterConvertCallback
import jakarta.annotation.PostConstruct

/**
 * Wires the Stripe-backed billing module.
 *
 * The outer class gates on classpath and sets Stripe.apiKey via @PostConstruct.
 * The inner [JdbcRepositoryConfig] enables JDBC repository scanning for the billing package.
 * The inner [BeansConfig] injects the repository and exposes [BillingService] and
 * [StripeWebhookHandler] as beans.
 *
 * Separating @EnableJdbcRepositories (inner class) from @Bean injection (another inner class)
 * avoids any potential circular dependency, following the pattern established in
 * [OrganizationAutoConfiguration].
 */
@AutoConfiguration
@ConditionalOnClass(name = ["com.stripe.Stripe"])
@ConditionalOnProperty(
    prefix = "saasstarter.billing",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@EnableConfigurationProperties(SaasStarterProperties::class)
class BillingAutoConfiguration(
    private val properties: SaasStarterProperties,
) {

    @PostConstruct
    fun configureStripe() {
        if (properties.billing.apiKey.isNotBlank()) {
            Stripe.apiKey = properties.billing.apiKey
        }
    }

    /**
     * Registers Spring Data JDBC repositories from the starter's billing package.
     * Gated on [ConditionalOnMissingBean] so unit tests (or consumers) can supply their
     * own [SubscriptionRepository] without conflicting with Spring Data's factory bean
     * registration — the standard Spring Boot "back-off" pattern.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(SubscriptionRepository::class)
    @EnableJdbcRepositories(basePackages = ["org.granchi.saasstarter.billing"])
    class JdbcRepositoryConfig

    @Configuration(proxyBeanMethods = false)
    class BeansConfig(private val properties: SaasStarterProperties) {

        @Bean
        @ConditionalOnMissingBean
        fun billingService(repo: SubscriptionRepository): BillingService =
            BillingService(repo, properties)

        @Bean
        @ConditionalOnMissingBean
        fun stripeWebhookHandler(repo: SubscriptionRepository): StripeWebhookHandler =
            StripeWebhookHandler(repo)

        @Bean
        fun subscriptionAfterConvertCallback(): AfterConvertCallback<Subscription> =
            AfterConvertCallback { entity ->
                entity._new = false
                entity
            }
    }
}
