import com.github.jk1.license.render.JsonReportRenderer

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    `java-library`
    `maven-publish`
    alias(libs.plugins.dependency.license.report)
}

group = "tech.sergiodelgado"
version = providers.gradleProperty("version").getOrElse("0.1.0-SNAPSHOT")

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

licenseReport {
    configurations = arrayOf("runtimeClasspath")
    renderers = arrayOf(JsonReportRenderer("licenses.json"))
}

dependencies {
    // Spring Boot — declared as `compileOnly` so consumers bring their own
    // version. They pick the Spring Boot version, we just use its APIs.
    compileOnly(platform(libs.spring.boot.bom))
    compileOnly(libs.spring.boot.starter)
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.boot.web)
    compileOnly(libs.spring.boot.security)
    compileOnly(libs.spring.boot.data.jdbc)
    compileOnly(libs.spring.boot.data.redis)
    compileOnly(libs.spring.boot.validation)
    compileOnly(libs.spring.session.redis)
    compileOnly(libs.jobrunr.spring)

    // JWT validation — bundled because it's our concrete choice
    api(libs.auth0.jwt)
    api(libs.auth0.jwks)

    // Konform — exposed in our public API (Validation<T>, DomainValidationException.errors)
    api(libs.konform)

    // Stripe Java SDK — exposed in public API (BillingService, StripeWebhookHandler signatures)
    api(libs.stripe)

    // Tests
    testImplementation(platform(libs.spring.boot.bom))
    testImplementation(libs.spring.boot.starter)
    testImplementation(libs.spring.boot.autoconfigure)
    testImplementation(libs.spring.boot.web)
    testImplementation(libs.spring.boot.security)
    testImplementation(libs.spring.boot.data.jdbc)
    testImplementation(libs.spring.boot.data.redis)
    testImplementation(libs.spring.boot.validation)
    testImplementation(libs.spring.session.redis)
    testImplementation(libs.jobrunr.spring)
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.mockk)
    testImplementation(libs.strikt.core)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.postgres)
    testImplementation(libs.postgresql)
    testImplementation(libs.testcontainers.postgres)
    testImplementation(libs.spring.boot.testcontainers)

    // Gradle 9's useJUnitPlatform() no longer auto-adds the launcher.
    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
}

// ── Publishing to GitHub Packages ─────────────────────────────────────────────

tasks.withType<PublishToMavenRepository>().configureEach {
    doFirst {
        check(System.getenv("GITHUB_ACTIONS") == "true") {
            "Publishing to GitHub Packages must go through the release-please CI workflow, not `./gradlew publish` locally."
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("Kotlin SaaS Starter")
                description.set("Opinionated transversal library for B2B SaaS in Kotlin + Spring Boot")
                url.set("https://github.com/sergiodelgadotech/kotlin-saas-starter")
                developers {
                    developer {
                        id.set("sergiodelgado")
                        name.set("Sergio Delgado")
                    }
                }
                scm {
                    url.set("https://github.com/sergiodelgadotech/kotlin-saas-starter")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sergiodelgadotech/kotlin-saas-starter")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR")).getOrElse("")
                password = providers.gradleProperty("gpr.token")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN")).getOrElse("")
            }
        }
    }
}

// ── NOTICE generation ─────────────────────────────────────────────────────────
tasks.register("generateNotice") {
    dependsOn("generateLicenseReport")

    val reportFile = layout.buildDirectory.file("reports/dependency-license/licenses.json")
    inputs.file(reportFile)
    outputs.file(file("NOTICE"))

    doLast {
        @Suppress("UNCHECKED_CAST")
        val json = groovy.json.JsonSlurper().parse(reportFile.get().asFile) as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val deps = (json["dependencies"] as? List<Map<String, Any?>>).orEmpty()

        val content = buildString {
            appendLine("kotlin-saas-starter")
            appendLine("Copyright (c) 2026 Sergio Delgado")
            appendLine()
            appendLine("This product includes the following third-party components:")
            deps.sortedBy { it["moduleName"] as? String ?: "" }.forEach { dep ->
                val name = dep["moduleName"] as? String ?: run {
                    logger.warn("generateNotice: skipping dependency with no moduleName: $dep")
                    return@forEach
                }
                val version = dep["moduleVersion"] as? String ?: ""
                val license = dep["moduleLicense"] as? String ?: "Unknown"
                val url     = dep["moduleLicenseUrl"] as? String
                appendLine()
                appendLine("------------------------------------------------------------------------")
                appendLine("$name:$version")
                appendLine("License: $license")
                if (url != null) appendLine(url)
            }
            appendLine("------------------------------------------------------------------------")
        }

        file("NOTICE").writeText(content)
        logger.lifecycle("NOTICE written to ${file("NOTICE").absolutePath}")
    }
}

tasks.named("build") {
    dependsOn("generateNotice")
}
