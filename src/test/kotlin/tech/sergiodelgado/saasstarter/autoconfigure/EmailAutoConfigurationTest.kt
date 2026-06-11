package tech.sergiodelgado.saasstarter.autoconfigure

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import tech.sergiodelgado.saasstarter.email.EmailService
import tech.sergiodelgado.saasstarter.email.ResendEmailService

class EmailAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(EmailAutoConfiguration::class.java))

    @Test
    fun `EmailService and ResendEmailService are registered`() {
        contextRunner.run { context ->
            expectThat(context.getBeansOfType(EmailService::class.java)).hasSize(1)
            expectThat(context.getBeansOfType(ResendEmailService::class.java)).hasSize(1)
        }
    }

    @Test
    fun `autoconfig is skipped when email enabled is false`() {
        contextRunner
            .withPropertyValues("saasstarter.email.enabled=false")
            .run { context ->
                expectThat(context.getBeansOfType(EmailService::class.java)).hasSize(0)
            }
    }

    @Test
    fun `from-name is combined with from-address as Name address format`() {
        contextRunner
            .withPropertyValues(
                "saasstarter.email.from-address=noreply@example.com",
                "saasstarter.email.from-name=My App",
            )
            .run { context ->
                val service = context.getBean(ResendEmailService::class.java)
                expectThat(service.defaultFrom).isEqualTo("My App <noreply@example.com>")
            }
    }

    @Test
    fun `from-address is used as-is when from-name is blank`() {
        contextRunner
            .withPropertyValues("saasstarter.email.from-address=noreply@example.com")
            .run { context ->
                val service = context.getBean(ResendEmailService::class.java)
                expectThat(service.defaultFrom).isEqualTo("noreply@example.com")
            }
    }

    @Test
    fun `consumer-provided EmailService bean takes precedence`() {
        contextRunner
            .withBean(EmailService::class.java, { object : EmailService {
                override fun send(message: tech.sergiodelgado.saasstarter.email.EmailMessage) = "custom"
            }})
            .run { context ->
                expectThat(context.getBeansOfType(EmailService::class.java)).hasSize(1)
                expectThat(context.getBeansOfType(ResendEmailService::class.java)).hasSize(0)
            }
    }
}
