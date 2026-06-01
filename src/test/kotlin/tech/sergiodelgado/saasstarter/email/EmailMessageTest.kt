package tech.sergiodelgado.saasstarter.email

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class EmailMessageTest {

    @Test
    fun `requires at least one body`() {
        assertThrows<IllegalArgumentException> {
            EmailMessage(to = "user@example.com", subject = "Hi", htmlBody = null, textBody = null)
        }
    }

    @Test
    fun `accepts html body only`() {
        val msg = EmailMessage(to = "user@example.com", subject = "Hi", htmlBody = "<p>Hi</p>")
        expectThat(msg.htmlBody).isEqualTo("<p>Hi</p>")
    }

    @Test
    fun `accepts text body only`() {
        val msg = EmailMessage(to = "user@example.com", subject = "Hi", textBody = "Hi")
        expectThat(msg.textBody).isEqualTo("Hi")
    }

    @Test
    fun `from defaults to null`() {
        val msg = EmailMessage(to = "user@example.com", subject = "Hi", textBody = "Hi")
        expectThat(msg.from).isEqualTo(null)
    }
}
