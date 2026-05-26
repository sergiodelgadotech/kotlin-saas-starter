package tech.sergiodelgado.saasstarter.autoconfigure

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isNotNull

class AutoConfigurationImportsTest {

    @Test
    fun `imports file lists SaasStarterAutoConfiguration`() {
        val resource = this::class.java.classLoader.getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        )
        expectThat(resource).isNotNull()

        val content = resource!!.readText()
        expectThat(content)
            .contains("tech.sergiodelgado.saasstarter.autoconfigure.SaasStarterAutoConfiguration")
    }

    @Test
    fun `imports file lists SessionAutoConfiguration`() {
        val resource = this::class.java.classLoader.getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        )
        expectThat(resource).isNotNull()
        expectThat(resource!!.readText())
            .contains("tech.sergiodelgado.saasstarter.autoconfigure.SessionAutoConfiguration")
    }

    @Test
    fun `imports file lists JobRunrAutoConfiguration`() {
        val resource = this::class.java.classLoader.getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        )
        expectThat(resource).isNotNull()
        expectThat(resource!!.readText())
            .contains("tech.sergiodelgado.saasstarter.autoconfigure.JobRunrAutoConfiguration")
    }

    @Test
    fun `imports file lists RedisAutoConfiguration`() {
        val resource = this::class.java.classLoader.getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        )
        expectThat(resource).isNotNull()
        expectThat(resource!!.readText())
            .contains("tech.sergiodelgado.saasstarter.autoconfigure.RedisAutoConfiguration")
    }

    @Test
    fun `imports file lists WebMvcAutoConfiguration`() {
        val resource = this::class.java.classLoader.getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        )
        expectThat(resource).isNotNull()
        expectThat(resource!!.readText())
            .contains("tech.sergiodelgado.saasstarter.autoconfigure.WebMvcAutoConfiguration")
    }

    @Test
    fun `imports file lists OrganizationAutoConfiguration`() {
        val resource = this::class.java.classLoader.getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        )
        expectThat(resource).isNotNull()
        expectThat(resource!!.readText())
            .contains("tech.sergiodelgado.saasstarter.autoconfigure.OrganizationAutoConfiguration")
    }

    @Test
    fun `imports file lists BillingAutoConfiguration`() {
        val resource = this::class.java.classLoader.getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        )
        expectThat(resource).isNotNull()
        expectThat(resource!!.readText())
            .contains("tech.sergiodelgado.saasstarter.autoconfigure.BillingAutoConfiguration")
    }

    @Test
    fun `imports file lists SecurityAutoConfiguration`() {
        val resource = this::class.java.classLoader.getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        )
        expectThat(resource).isNotNull()
        expectThat(resource!!.readText())
            .contains("tech.sergiodelgado.saasstarter.autoconfigure.SecurityAutoConfiguration")
    }

    @Test
    fun `imports file lists WebAutoConfiguration`() {
        val resource = this::class.java.classLoader.getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        )
        expectThat(resource).isNotNull()
        expectThat(resource!!.readText())
            .contains("tech.sergiodelgado.saasstarter.autoconfigure.WebAutoConfiguration")
    }
}
