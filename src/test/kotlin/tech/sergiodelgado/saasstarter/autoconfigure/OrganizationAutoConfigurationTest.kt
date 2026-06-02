package tech.sergiodelgado.saasstarter.autoconfigure

import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isA
import strikt.assertions.isFalse
import tech.sergiodelgado.saasstarter.lock.RedisLockService
import tech.sergiodelgado.saasstarter.organization.Member
import tech.sergiodelgado.saasstarter.organization.MemberRepository
import tech.sergiodelgado.saasstarter.organization.Organization
import tech.sergiodelgado.saasstarter.organization.OrganizationRepository
import tech.sergiodelgado.saasstarter.organization.OrganizationService
import java.util.UUID

class OrganizationAutoConfigurationTest {

    private val beansConfig = OrganizationAutoConfiguration.BeansConfig()

    @Test
    fun `imports file lists OrganizationAutoConfiguration`() {
        val resource = this::class.java.classLoader.getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        )
        expectThat(resource!!.readText())
            .contains("tech.sergiodelgado.saasstarter.autoconfigure.OrganizationAutoConfiguration")
    }

    @Test
    fun `organizationService factory method creates an OrganizationService`() {
        val service = beansConfig.organizationService(
            mockk<OrganizationRepository>(),
            mockk<MemberRepository>(),
            mockk<RedisLockService>(),
            mockk<ApplicationEventPublisher>(),
        )
        expectThat(service).isA<OrganizationService>()
    }

    @Test
    fun `organizationAfterConvertCallback sets _new to false`() {
        val callback = beansConfig.organizationAfterConvertCallback()
        val org = Organization(id = UUID.randomUUID(), name = "Acme", slug = "acme")
        callback.onAfterConvert(org)
        expectThat(org._new).isFalse()
    }

    @Test
    fun `memberAfterConvertCallback sets _new to false`() {
        val callback = beansConfig.memberAfterConvertCallback()
        val member = Member(organizationId = UUID.randomUUID(), externalUserId = "user-1")
        callback.onAfterConvert(member)
        expectThat(member._new).isFalse()
    }
}
