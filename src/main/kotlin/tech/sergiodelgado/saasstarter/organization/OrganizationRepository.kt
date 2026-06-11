package tech.sergiodelgado.saasstarter.organization

import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface OrganizationRepository : CrudRepository<Organization, UUID> {
    fun findBySlug(slug: String): Organization?
}

interface MemberRepository : CrudRepository<Member, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<Member>
    fun findByExternalUserId(externalUserId: String): Member?

    /**
     * Cached — runs on every authenticated request.
     * Eviction is handled by OrganizationService.removeMember.
     */
    // Returns the UUID as String so Redis can cache it safely — scalar JSON strings
    // deserialize as String regardless of typing configuration (no @class wrapper possible).
    // Callers convert to UUID via UUID.fromString().
    @Cacheable("tenant-by-user", key = "#userId")
    @Query("SELECT organization_id FROM members WHERE external_user_id = :userId")
    fun findOrganizationIdByUserId(userId: String): String?

    fun existsByOrganizationIdAndExternalUserId(organizationId: UUID, externalUserId: String): Boolean

    @Modifying
    @Query("UPDATE members SET email = :email, first_name = :firstName, last_name = :lastName WHERE external_user_id = :externalUserId")
    fun updateProfile(externalUserId: String, email: String?, firstName: String?, lastName: String?)
}
