package tech.sergiodelgado.saasstarter.auth.idp

/**
 * IdP-agnostic interface for user lifecycle operations that the app drives.
 *
 * Implementations (e.g. Zitadel) translate these calls into IdP-specific API requests.
 */
interface IdpUserDirectory {
    /**
     * Returns the IdP subject (`sub`) for the given email.
     * Creates the IdP user (and triggers an invitation email) if one does not exist.
     */
    fun findOrInvite(email: String): String

    /**
     * Updates the display name of an existing IdP user.
     *
     * @param userId the IdP subject (`sub`) of the user to update
     * @param givenName new first name (must be non-blank)
     * @param familyName new last name (must be non-blank)
     */
    fun updateProfile(userId: String, givenName: String, familyName: String)
}
