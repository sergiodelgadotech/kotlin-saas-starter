package tech.sergiodelgado.saasstarter.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import tech.sergiodelgado.saasstarter.security.ZitadelSessionBridgeFilter

/**
 * Wires [ZitadelSessionBridgeFilter] and [OidcClientInitiatedLogoutSuccessHandler] when
 * `spring-security-oauth2-client` is on the classpath. Backs off via [ConditionalOnMissingBean]
 * so consumers can supply their own implementations.
 */
@AutoConfiguration
@ConditionalOnClass(OidcUser::class, ClientRegistrationRepository::class)
@EnableConfigurationProperties(SaasStarterProperties::class)
class ZitadelOidcAutoConfiguration(private val properties: SaasStarterProperties) {

    @Bean
    @ConditionalOnMissingBean
    fun zitadelSessionBridgeFilter(): ZitadelSessionBridgeFilter = ZitadelSessionBridgeFilter()

    @Bean
    @ConditionalOnMissingBean(OidcClientInitiatedLogoutSuccessHandler::class)
    @ConditionalOnBean(ClientRegistrationRepository::class)
    fun oidcLogoutSuccessHandler(
        clientRegistrationRepository: ClientRegistrationRepository,
    ): OidcClientInitiatedLogoutSuccessHandler =
        OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository).also {
            it.setPostLogoutRedirectUri(properties.security.postLogoutRedirectUri)
        }
}
