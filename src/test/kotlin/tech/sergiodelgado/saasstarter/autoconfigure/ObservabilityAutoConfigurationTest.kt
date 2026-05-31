package tech.sergiodelgado.saasstarter.autoconfigure

import io.micrometer.observation.ObservationRegistry
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.core.Ordered
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue
import tech.sergiodelgado.saasstarter.observability.CorrelationIdFilter
import tech.sergiodelgado.saasstarter.observability.TenantMdcInterceptor
import tech.sergiodelgado.saasstarter.observability.TenantObservationFilter

class ObservabilityAutoConfigurationTest {

    private val contextRunner = WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ObservabilityAutoConfiguration::class.java))

    @Test
    fun `registers CorrelationIdFilter, TenantMdcInterceptor, and TenantObservationFilter beans when Micrometer and ObservationRegistry are present`() {
        contextRunner
            .withBean(ObservationRegistry::class.java, { ObservationRegistry.create() })
            .run { context ->
                val filterBeans = context.getBeansOfType(FilterRegistrationBean::class.java)
                val correlationFilters = filterBeans.values.filter { it.filter is CorrelationIdFilter }
                expectThat(correlationFilters).hasSize(1)
                expectThat(context.getBeansOfType(TenantMdcInterceptor::class.java)).hasSize(1)
                expectThat(context.getBeansOfType(TenantObservationFilter::class.java)).hasSize(1)
            }
    }

    @Test
    fun `registers all beans including TenantObservationFilter when Micrometer is on the classpath`() {
        contextRunner.run { context ->
            val filterBeans = context.getBeansOfType(FilterRegistrationBean::class.java)
            val correlationFilters = filterBeans.values.filter { it.filter is CorrelationIdFilter }
            expectThat(correlationFilters).hasSize(1)
            expectThat(context.getBeansOfType(TenantMdcInterceptor::class.java)).hasSize(1)
            expectThat(context.getBeansOfType(TenantObservationFilter::class.java)).hasSize(1)
        }
    }

    @Test
    fun `ObservabilityAutoConfiguration is itself a WebMvcConfigurer`() {
        contextRunner.run { context ->
            val configurers = context.getBeansOfType(WebMvcConfigurer::class.java)
            expectThat(configurers.values.any { it is ObservabilityAutoConfiguration }).isTrue()
        }
    }

    @Test
    fun `CorrelationIdFilter is registered with HIGHEST_PRECEDENCE order`() {
        contextRunner.run { context ->
            val filterBeans = context.getBeansOfType(FilterRegistrationBean::class.java)
            val correlationFilterBean = filterBeans.values.first { it.filter is CorrelationIdFilter }
            expectThat(correlationFilterBean.order).isEqualTo(Ordered.HIGHEST_PRECEDENCE)
        }
    }

    @Test
    fun `addInterceptors registers TenantMdcInterceptor`() {
        val tenantMdcInterceptor = TenantMdcInterceptor()
        val config = ObservabilityAutoConfiguration(interceptorProvider(tenantMdcInterceptor))
        val registry = mockk<InterceptorRegistry>(relaxed = true)

        config.addInterceptors(registry)

        verify { registry.addInterceptor(tenantMdcInterceptor) }
    }

    private fun <T : Any> interceptorProvider(value: T): ObjectProvider<T> =
        object : ObjectProvider<T> {
            override fun getObject(): T = value
            override fun getObject(vararg args: Any?): T = value
            override fun getIfAvailable(): T = value
            override fun getIfUnique(): T = value
            override fun iterator(): MutableIterator<T> = mutableListOf(value).iterator()
        }
}
