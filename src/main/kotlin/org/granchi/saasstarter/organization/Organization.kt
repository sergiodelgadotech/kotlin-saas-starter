package org.granchi.saasstarter.organization

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * Spring Data JDBC considers an entity "new" (INSERT) only when the @Id field is null.
 * Since we pre-generate UUIDs, we implement Persistable<UUID> and carry a @Transient
 * _new flag so that freshly constructed entities are always inserted.
 *
 * The Kotlin property `val id` already satisfies `Persistable.getId()` via its
 * generated JVM getter, so we only need to declare `isNew()`.
 */
@Table("organizations")
data class Organization(
    @Id @get:JvmName("entityId") val id: UUID = UUID.randomUUID(),
    val name: String,
    val slug: String,
    val plan: String = "starter",
    val createdAt: Instant = Instant.now(),
) : Persistable<UUID> {
    /**
     * Tracks whether this instance has been persisted. Starts as true (INSERT on first save).
     * Spring Data JDBC calls AfterConvertCallback or @PostLoad to flip this — but since
     * we use @Transient as a body property (not a constructor param), it defaults to true
     * for freshly constructed instances and must be set to false via [markPersisted] after loading.
     */
    @Transient
    @JvmField
    internal var _new: Boolean = true

    override fun getId(): UUID = id
    override fun isNew(): Boolean = _new
}

@Table("members")
data class Member(
    @Id @get:JvmName("entityId") val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    /** Identity provider's user ID. App's TenantResolver maps it to the org. */
    val externalUserId: String,
    val role: String = DefaultMemberRole.MEMBER.name,
    val createdAt: Instant = Instant.now(),
) : Persistable<UUID> {
    /**
     * See [Organization._new] for details.
     */
    @Transient
    @JvmField
    internal var _new: Boolean = true

    override fun getId(): UUID = id
    override fun isNew(): Boolean = _new
}
