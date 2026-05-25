package org.granchi.saasstarter.autoconfigure

import org.granchi.saasstarter.web.GlobalExceptionHandler
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Wires [GlobalExceptionHandler] as a bean so it is discovered by Spring MVC's
 * advice resolution without requiring a component-scan of the starter's package.
 *
 * The handler returns Thymeleaf view names for error rendering. Consumers must
 * provide templates/error/{403,404,422,500}.html in their own resources.
 *
 * A future REST [ProblemDetail] variant (see #25) can be added here behind its own
 * [ConditionalOnProperty] without further refactor.
 *
 * Disable via [saasstarter.web.exception-handler.enabled=false] or override by
 * declaring your own [GlobalExceptionHandler] bean.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(WebMvcConfigurer::class)
@EnableConfigurationProperties(SaasStarterProperties::class)
class WebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "saasstarter.web.exception-handler",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true,
    )
    fun globalExceptionHandler(): GlobalExceptionHandler = GlobalExceptionHandler()
}
