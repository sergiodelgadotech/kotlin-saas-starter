package tech.sergiodelgado.saasstarter.email

data class EmailMessage(
    val to: String,
    val subject: String,
    val htmlBody: String? = null,
    val textBody: String? = null,
    val from: String? = null,
) {
    init {
        require(htmlBody != null || textBody != null) {
            "EmailMessage requires at least one of htmlBody or textBody"
        }
    }
}
