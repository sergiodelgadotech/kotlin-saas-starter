package tech.sergiodelgado.saasstarter.autoconfigure

import tech.sergiodelgado.saasstarter.lock.RedisLockService
import tech.sergiodelgado.saasstarter.organization.Member
import tech.sergiodelgado.saasstarter.organization.MemberRepository
import tech.sergiodelgado.saasstarter.organization.Organization
import tech.sergiodelgado.saasstarter.organization.OrganizationRepository
import tech.sergiodelgado.saasstarter.organization.OrganizationService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.data.relational.core.mapping.event.AfterConvertCallback

/**
 * Wires the multi-tenant Organization/Member model.
 *
 * The outer class gates on classpath and sets up JDBC repository scanning via
 * [JdbcRepositoryConfig]. The inner [BeansConfig] injects the repositories once
 * they are registered and exposes [OrganizationService] as a bean.
 *
 * Separating @EnableJdbcRepositories (outer) from @Bean injection (inner) avoids
 * the circular dependency that would arise if the class that declares repositories
 * also constructor-injects them.
 *
 * AfterConvertCallback beans flip [Organization._new] and [Member._new] to false
 * after any entity is loaded from the database, ensuring subsequent save() calls
 * perform UPDATE rather than INSERT.
 */
@AutoConfiguration(after = [RedisAutoConfiguration::class])
@ConditionalOnClass(name = ["org.springframework.data.jdbc.repository.config.EnableJdbcRepositories"])
class OrganizationAutoConfiguration {

    /**
     * Registers Spring Data JDBC repositories from the starter's organization package.
     * Must be a separate @Configuration so that the repositories are registered before
     * [BeansConfig] tries to inject them.
     */
    @Configuration(proxyBeanMethods = false)
    @EnableJdbcRepositories(basePackages = ["tech.sergiodelgado.saasstarter.organization"])
    class JdbcRepositoryConfig

    @Configuration(proxyBeanMethods = false)
    class BeansConfig {

        @Bean
        @ConditionalOnMissingBean
        fun organizationService(
            organizationRepository: OrganizationRepository,
            memberRepository: MemberRepository,
            lockService: RedisLockService,
        ): OrganizationService =
            OrganizationService(organizationRepository, memberRepository, lockService)

        @Bean
        fun organizationAfterConvertCallback(): AfterConvertCallback<Organization> =
            AfterConvertCallback { entity ->
                entity._new = false
                entity
            }

        @Bean
        fun memberAfterConvertCallback(): AfterConvertCallback<Member> =
            AfterConvertCallback { entity ->
                entity._new = false
                entity
            }
    }
}
