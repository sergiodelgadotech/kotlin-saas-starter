package org.granchi.saasstarter.autoconfigure

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
            .contains("org.granchi.saasstarter.autoconfigure.SaasStarterAutoConfiguration")
    }
}
