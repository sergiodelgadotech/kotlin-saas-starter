package tech.sergiodelgado.saasstarter.email

import com.resend.Resend
import com.resend.core.exception.ResendException
import com.resend.services.emails.Emails
import com.resend.services.emails.model.CreateEmailOptions
import com.resend.services.emails.model.CreateEmailResponse
import io.micrometer.observation.tck.TestObservationRegistry
import io.micrometer.observation.tck.TestObservationRegistryAssert
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ResendEmailServiceTest {

    private val observationRegistry = TestObservationRegistry.create()
    private val resendEmails = mockk<Emails>()
    private val resend = mockk<Resend> { every { emails() } returns resendEmails }
    private val service = ResendEmailService(resend, "default@example.com", observationRegistry)

    @Test
    fun `sends email and returns provider message id`() {
        val response = mockk<CreateEmailResponse> { every { id } returns "msg_abc" }
        every { resendEmails.send(any<CreateEmailOptions>()) } returns response

        val id = service.send(EmailMessage(to = "user@example.com", subject = "Test", textBody = "Hello"))

        expectThat(id).isEqualTo("msg_abc")
    }

    @Test
    fun `uses message from address when provided`() {
        val optionsSlot = slot<CreateEmailOptions>()
        val stub = mockk<CreateEmailResponse> { every { id } returns "x" }
        every { resendEmails.send(capture(optionsSlot)) } returns stub

        service.send(EmailMessage(to = "user@example.com", subject = "Test", textBody = "Hi", from = "custom@example.com"))

        expectThat(optionsSlot.captured.from).isEqualTo("custom@example.com")
    }

    @Test
    fun `falls back to defaultFrom when message from is null`() {
        val optionsSlot = slot<CreateEmailOptions>()
        val stub = mockk<CreateEmailResponse> { every { id } returns "x" }
        every { resendEmails.send(capture(optionsSlot)) } returns stub

        service.send(EmailMessage(to = "user@example.com", subject = "Test", textBody = "Hi"))

        expectThat(optionsSlot.captured.from).isEqualTo("default@example.com")
    }

    @Test
    fun `maps text and html bodies to Resend options`() {
        val optionsSlot = slot<CreateEmailOptions>()
        val stub = mockk<CreateEmailResponse> { every { id } returns "x" }
        every { resendEmails.send(capture(optionsSlot)) } returns stub

        service.send(EmailMessage(to = "user@example.com", subject = "Greet", textBody = "Hello", htmlBody = "<b>Hello</b>"))

        expectThat(optionsSlot.captured.text).isEqualTo("Hello")
        expectThat(optionsSlot.captured.html).isEqualTo("<b>Hello</b>")
    }

    @Test
    fun `records observation with outcome sent on success`() {
        every { resendEmails.send(any<CreateEmailOptions>()) } returns mockk<CreateEmailResponse> { every { id } returns "ok" }

        service.send(EmailMessage(to = "user@example.com", subject = "Test", textBody = "Hello"))

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasObservationWithNameEqualTo("saasstarter.email.send")
            .that()
            .hasLowCardinalityKeyValue("outcome", "sent")
    }

    @Test
    fun `throws ResendException and records observation with outcome failed`() {
        every { resendEmails.send(any<CreateEmailOptions>()) } throws ResendException("API error")

        assertThrows<ResendException> {
            service.send(EmailMessage(to = "user@example.com", subject = "Test", textBody = "Hello"))
        }

        TestObservationRegistryAssert.assertThat(observationRegistry)
            .hasObservationWithNameEqualTo("saasstarter.email.send")
            .that()
            .hasLowCardinalityKeyValue("outcome", "failed")
    }
}
