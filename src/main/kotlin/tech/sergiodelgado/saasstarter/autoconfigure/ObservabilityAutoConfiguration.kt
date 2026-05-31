package tech.sergiodelgado.saasstarter.autoconfigure

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import tech.sergiodelgado.saasstarter.observability.CorrelationIdFilter
import tech.sergiodelgado.saasstarter.observability.TenantMdcInterceptor
import tech.sergiodelgado.saasstarter.observability.TenantObservationFilter

/**
 * Auto-configuration for observability support:
 * - [CorrelationIdFilter]: propagates/generates X-Correlation-Id header and writes it to MDC
 * - [TenantMdcInterceptor]: writes tenant ID to MDC for controller-scoped logs
 * - [TenantObservationFilter]: tags Micrometer observations with the current tenant ID
 *   (conditional on Micrometer's ObservationFilter being on the classpath)
 */
@AutoConfiguration
@AutoConfigureAfter(WebMvcAutoConfiguration::class)
@ConditionalOnWebApplication(type = SERVLET)
class ObservabilityAutoConfiguration(
    private val tenantMdcInterceptorProvider: ObjectProvider<TenantMdcInterceptor>,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        tenantMdcInterceptorProvider.ifAvailable { registry.addInterceptor(it) }
    }

    @Configuration(proxyBeanMethods = false)
    class MdcBeansConfig {

        @Bean(name = ["saasStarterCorrelationIdFilter"])
        fun correlationIdFilter(): FilterRegistrationBean<CorrelationIdFilter> =
            FilterRegistrationBean(CorrelationIdFilter()).apply {
                order = Ordered.HIGHEST_PRECEDENCE
            }

        @Bean(name = ["saasStarterTenantMdcInterceptor"])
        fun tenantMdcInterceptor(): TenantMdcInterceptor = TenantMdcInterceptor()
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = ["io.micrometer.observation.ObservationFilter"])
    class MicrometerBeansConfig {

        @Bean(name = ["saasStarterTenantObservationFilter"])
        fun tenantObservationFilter(): TenantObservationFilter = TenantObservationFilter()
    }
}
