package tech.sergiodelgado.saasstarter.email

interface EmailService {
    /** Sends [message] and returns the provider-assigned message ID. Throws on failure. */
    fun send(message: EmailMessage): String
}
