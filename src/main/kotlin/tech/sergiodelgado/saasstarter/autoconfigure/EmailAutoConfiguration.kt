package tech.sergiodelgado.saasstarter.autoconfigure

import com.resend.Resend
import io.micrometer.observation.ObservationRegistry
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tech.sergiodelgado.saasstarter.email.EmailService
import tech.sergiodelgado.saasstarter.email.ResendEmailService

@AutoConfiguration
@ConditionalOnClass(name = ["com.resend.Resend"])
@ConditionalOnProperty(
    prefix = "saasstarter.email",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@EnableConfigurationProperties(SaasStarterProperties::class)
class EmailAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    class BeansConfig(private val properties: SaasStarterProperties) {

        @Bean
        @ConditionalOnMissingBean
        fun resendClient(): Resend = Resend(properties.email.apiKey)

        @Bean
        @ConditionalOnMissingBean
        fun emailService(
            resend: Resend,
            observationRegistry: ObjectProvider<ObservationRegistry>,
        ): EmailService = ResendEmailService(
            resend,
            properties.email.fromAddress,
            observationRegistry.getIfAvailable { ObservationRegistry.NOOP },
        )
    }
}
