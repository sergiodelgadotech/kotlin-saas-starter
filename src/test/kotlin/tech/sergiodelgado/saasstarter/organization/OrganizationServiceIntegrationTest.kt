package tech.sergiodelgado.saasstarter.organization

import com.ninjasquad.springmockk.MockkBean
import tech.sergiodelgado.saasstarter.lock.RedisLockService
import tech.sergiodelgado.saasstarter.test.TestBootApp
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@Tag("integration")
@Testcontainers
@Transactional
@SpringBootTest(
    classes = [TestBootApp::class],
    properties = [
        "spring.flyway.locations=classpath:db/migration/saasstarter",
        "saasstarter.cache.enabled=false",
        "spring.autoconfigure.exclude=tech.sergiodelgado.saasstarter.autoconfigure.WebMvcAutoConfiguration",
    ],
)
class OrganizationServiceIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine")
    }

    @MockkBean
    lateinit var redisLockService: RedisLockService

    @Autowired
    lateinit var organizationRepository: OrganizationRepository

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Test
    fun `Flyway migration creates organizations and members tables`() {
        val org = organizationRepository.save(Organization(name = "Acme", slug = "acme-integration"))
        val member = memberRepository.save(
            Member(organizationId = org.id, externalUserId = "user-integration-123")
        )
        expectThat(memberRepository.findByExternalUserId("user-integration-123")?.id)
            .isEqualTo(member.id)
    }

    @Test
    fun `findOrganizationIdByUserId returns the org for a member`() {
        val org = organizationRepository.save(Organization(name = "Beta", slug = "beta-integration"))
        memberRepository.save(Member(organizationId = org.id, externalUserId = "user-integration-456"))

        val resolvedOrgId = memberRepository.findOrganizationIdByUserId("user-integration-456")
        expectThat(resolvedOrgId).isNotNull().isEqualTo(org.id.toString())
    }
}
