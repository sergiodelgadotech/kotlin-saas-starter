package tech.sergiodelgado.saasstarter.email

import com.resend.Resend
import com.resend.services.emails.model.CreateEmailOptions
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.slf4j.LoggerFactory

open class ResendEmailService(
    private val resend: Resend,
    internal val defaultFrom: String,
    private val observationRegistry: ObservationRegistry = ObservationRegistry.NOOP,
) : EmailService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(message: EmailMessage): String {
        var outcome = "sent"
        val obs = Observation.createNotStarted("saasstarter.email.send", observationRegistry).start()
        try {
            var builder = CreateEmailOptions.builder()
                .from(message.from ?: defaultFrom)
                .to(message.to)
                .subject(message.subject)
            message.textBody?.let { builder = builder.text(it) }
            message.htmlBody?.let { builder = builder.html(it) }
            val response = resend.emails().send(builder.build())
            log.debug("Email sent to ${message.to}, id=${response.id}")
            return response.id
        } catch (e: Exception) {
            outcome = "failed"
            obs.error(e)
            log.warn("Failed to send email to ${message.to}: ${e.message}")
            throw e
        } finally {
            obs.lowCardinalityKeyValue("outcome", outcome).stop()
        }
    }
}
