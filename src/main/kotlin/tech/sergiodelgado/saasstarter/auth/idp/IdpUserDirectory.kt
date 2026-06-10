package tech.sergiodelgado.saasstarter.auth.idp

/**
 * IdP-agnostic interface for resolving an IdP subject from an email address.
 *
 * Implementations (e.g. Zitadel) look up the user in the IdP by email and return
 * the `sub` claim. If no user exists, they create one and trigger the IdP's
 * invitation email, then return the new subject.
 *
 * Being a `fun interface` allows test stubs to be written as lambdas.
 */
fun interface IdpUserDirectory {
    /**
     * Returns the IdP subject (`sub`) for the given email.
     * Creates the IdP user (and triggers an invitation email) if one does not exist.
     */
    fun findOrInvite(email: String): String
}
