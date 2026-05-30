package tech.sergiodelgado.saasstarter.organization

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import tech.sergiodelgado.saasstarter.lock.RedisLockService
import tech.sergiodelgado.saasstarter.tenant.TenantContext
import tech.sergiodelgado.saasstarter.web.NotFoundException
import java.util.Optional
import java.util.UUID

class OrganizationServiceTest {

    private val orgRepo = mockk<OrganizationRepository>()
    private val memberRepo = mockk<MemberRepository>()
    private val lockService = mockk<RedisLockService>()
    private val service = OrganizationService(orgRepo, memberRepo, lockService)
    private val orgId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        TenantContext.set(orgId)
    }

    @AfterEach
    fun tearDown() {
        TenantContext.clear()
    }

    @Test
    fun `current returns organization when found`() {
        val org = Organization(id = orgId, name = "Acme", slug = "acme")
        every { orgRepo.findById(orgId) } returns Optional.of(org)
        expectThat(service.current()).isEqualTo(org)
    }

    @Test
    fun `current throws NotFoundException when organization not found`() {
        every { orgRepo.findById(orgId) } returns Optional.empty()
        assertThrows<NotFoundException> { service.current() }
    }

    @Test
    fun `members returns list from repository`() {
        val members = listOf(Member(organizationId = orgId, externalUserId = "user-1"))
        every { memberRepo.findByOrganizationId(orgId) } returns members
        expectThat(service.members()).isEqualTo(members)
    }

    @Test
    fun `inviteMember saves new member when not already a member`() {
        val userId = "user-ext-1"
        val savedMember = Member(organizationId = orgId, externalUserId = userId)
        every { lockService.withLock<Member>(any(), any(), any()) } answers {
            @Suppress("UNCHECKED_CAST")
            (args[2] as Function0<Member>).invoke()
        }
        every { memberRepo.existsByOrganizationIdAndExternalUserId(orgId, userId) } returns false
        every { memberRepo.save(any()) } returns savedMember

        expectThat(service.inviteMember(userId)).isEqualTo(savedMember)
    }

    @Test
    fun `inviteMember throws when user is already a member`() {
        val userId = "user-dup"
        every { lockService.withLock<Member>(any(), any(), any()) } answers {
            @Suppress("UNCHECKED_CAST")
            (args[2] as Function0<Member>).invoke()
        }
        every { memberRepo.existsByOrganizationIdAndExternalUserId(orgId, userId) } returns true

        assertThrows<IllegalStateException> { service.inviteMember(userId) }
    }

    @Test
    fun `removeMember deletes and returns the member`() {
        val memberId = UUID.randomUUID()
        val member = Member(id = memberId, organizationId = orgId, externalUserId = "user-rem")
        every { memberRepo.findById(memberId) } returns Optional.of(member)
        every { memberRepo.delete(member) } just Runs

        expectThat(service.removeMember(memberId)).isEqualTo(member)
        verify { memberRepo.delete(member) }
    }

    @Test
    fun `removeMember throws NotFoundException when member not found`() {
        val memberId = UUID.randomUUID()
        every { memberRepo.findById(memberId) } returns Optional.empty()

        assertThrows<NotFoundException> { service.removeMember(memberId) }
    }

    @Test
    fun `removeMember throws when member belongs to a different organization`() {
        val memberId = UUID.randomUUID()
        val otherOrgId = UUID.randomUUID()
        val member = Member(id = memberId, organizationId = otherOrgId, externalUserId = "user-other")
        every { memberRepo.findById(memberId) } returns Optional.of(member)

        assertThrows<IllegalStateException> { service.removeMember(memberId) }
    }

    @Test
    fun `updateName saves organization with new name`() {
        val org = Organization(id = orgId, name = "Old Name", slug = "old")
        every { orgRepo.findById(orgId) } returns Optional.of(org)
        every { orgRepo.save(any()) } answers { args[0] as Organization }

        val result = service.updateName("New Name")
        expectThat(result.name).isEqualTo("New Name")
    }
}
