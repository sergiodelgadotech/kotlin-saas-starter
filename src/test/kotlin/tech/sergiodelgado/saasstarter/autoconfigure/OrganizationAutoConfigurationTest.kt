package tech.sergiodelgado.saasstarter.autoconfigure

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains

class OrganizationAutoConfigurationTest {

    @Test
    fun `imports file lists OrganizationAutoConfiguration`() {
        val resource = this::class.java.classLoader.getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        )
        expectThat(resource!!.readText())
            .contains("tech.sergiodelgado.saasstarter.autoconfigure.OrganizationAutoConfiguration")
    }
}
