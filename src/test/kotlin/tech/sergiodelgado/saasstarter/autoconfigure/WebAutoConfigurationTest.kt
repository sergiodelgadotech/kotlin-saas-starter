package tech.sergiodelgado.saasstarter.autoconfigure

import tech.sergiodelgado.saasstarter.web.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import strikt.api.expectThat
import strikt.assertions.hasSize

class WebAutoConfigurationTest {

    private val contextRunner = WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(WebAutoConfiguration::class.java))

    @Test
    fun `GlobalExceptionHandler bean is registered by default`() {
        contextRunner.run { context ->
            expectThat(context.getBeansOfType(GlobalExceptionHandler::class.java)).hasSize(1)
        }
    }

    @Test
    fun `GlobalExceptionHandler is skipped when exception-handler is disabled`() {
        contextRunner
            .withPropertyValues("saasstarter.web.exception-handler.enabled=false")
            .run { context ->
                expectThat(context.getBeansOfType(GlobalExceptionHandler::class.java)).hasSize(0)
            }
    }

    @Test
    fun `GlobalExceptionHandler backs off when consumer provides own bean`() {
        contextRunner
            .withBean(GlobalExceptionHandler::class.java, { GlobalExceptionHandler() })
            .run { context ->
                expectThat(context.getBeansOfType(GlobalExceptionHandler::class.java)).hasSize(1)
            }
    }
}
