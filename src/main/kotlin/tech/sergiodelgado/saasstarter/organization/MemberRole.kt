package tech.sergiodelgado.saasstarter.organization

/**
 * Marker interface for member roles. Apps replace [DefaultMemberRole] with their
 * own enum implementing this interface when the default OWNER/ADMIN/MEMBER set
 * isn't sufficient.
 */
interface MemberRole {
    /** Stable identifier used for persistence and serialization. */
    val name: String
}

enum class DefaultMemberRole : MemberRole {
    OWNER,
    ADMIN,
    MEMBER;
}
