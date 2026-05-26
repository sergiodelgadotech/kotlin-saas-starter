package tech.sergiodelgado.saasstarter.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties

/**
 * Top-level autoconfiguration for kotlin-saas-starter. Subsequent plans add
 * focused autoconfig classes (e.g. SessionAutoConfiguration) declared in the
 * AutoConfiguration.imports file alongside this one.
 */
@AutoConfiguration
@EnableConfigurationProperties(SaasStarterProperties::class)
class SaasStarterAutoConfiguration
