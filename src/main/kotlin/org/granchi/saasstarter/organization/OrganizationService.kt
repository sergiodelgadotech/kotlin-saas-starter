package org.granchi.saasstarter.organization

import org.granchi.saasstarter.lock.RedisLockService
import org.granchi.saasstarter.tenant.TenantContext
import org.granchi.saasstarter.web.NotFoundException
import org.springframework.cache.annotation.CacheEvict
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
open class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val memberRepository: MemberRepository,
    private val lockService: RedisLockService,
) {

    fun current(): Organization =
        organizationRepository.findById(TenantContext.get()).orElseThrow {
            NotFoundException("Organization not found")
        }

    fun members(): List<Member> =
        memberRepository.findByOrganizationId(TenantContext.get())

    fun inviteMember(externalUserId: String, role: String = DefaultMemberRole.MEMBER.name): Member {
        val orgId = TenantContext.get()
        return lockService.withLock("invite:$orgId:$externalUserId") {
            check(!memberRepository.existsByOrganizationIdAndExternalUserId(orgId, externalUserId)) {
                "User is already a member of this organization"
            }
            memberRepository.save(
                Member(organizationId = orgId, externalUserId = externalUserId, role = role)
            )
        }
    }

    @CacheEvict("tenant-by-user", key = "#result.externalUserId", condition = "#result != null")
    open fun removeMember(memberId: UUID): Member {
        val member = memberRepository.findById(memberId).orElseThrow {
            NotFoundException("Member not found")
        }
        check(member.organizationId == TenantContext.get()) {
            "Member does not belong to current organization"
        }
        memberRepository.delete(member)
        return member
    }

    fun updateName(name: String): Organization {
        val org = current()
        val updated = org.copy(name = name)
        updated._new = false
        return organizationRepository.save(updated)
    }
}
