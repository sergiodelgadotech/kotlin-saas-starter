package org.granchi.saasstarter.organization

import org.springframework.cache.annotation.Cacheable
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
    @Cacheable("tenant-by-user", key = "#userId")
    @Query("SELECT organization_id FROM members WHERE external_user_id = :userId")
    fun findOrganizationIdByUserId(userId: String): UUID?

    fun existsByOrganizationIdAndExternalUserId(organizationId: UUID, externalUserId: String): Boolean
}
