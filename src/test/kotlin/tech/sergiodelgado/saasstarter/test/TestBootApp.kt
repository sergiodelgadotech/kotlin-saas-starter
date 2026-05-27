package tech.sergiodelgado.saasstarter.test

import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Minimal Spring Boot application for integration tests.
 *
 * Lives in a package that contains NO Spring Data repositories, so that
 * Spring Boot's JdbcRepositoriesAutoConfiguration scans this (empty) package
 * and registers nothing. The starter's own AutoConfigurations (e.g.
 * OrganizationAutoConfiguration) register their repositories explicitly via
 * @EnableJdbcRepositories, avoiding duplicate bean-definition conflicts.
 */
@SpringBootApplication
class TestBootApp
