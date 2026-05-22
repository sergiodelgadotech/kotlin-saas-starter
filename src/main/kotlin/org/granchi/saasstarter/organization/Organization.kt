package org.granchi.saasstarter.organization

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("organizations")
data class Organization(
    @Id val id: UUID = UUID.randomUUID(),
    val name: String,
    val slug: String,
    val plan: String = "starter",
    val createdAt: Instant = Instant.now(),
)

@Table("members")
data class Member(
    @Id val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    /** Identity provider's user ID. App's TenantResolver maps it to the org. */
    val externalUserId: String,
    val role: String = DefaultMemberRole.MEMBER.name,
    val createdAt: Instant = Instant.now(),
)
