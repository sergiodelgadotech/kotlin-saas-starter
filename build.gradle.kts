plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dep)
    `java-library`
    `maven-publish`
}

group = "org.granchi"
version = providers.gradleProperty("version").getOrElse("0.1.0-SNAPSHOT")

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.1")
    }
}

dependencies {
    // Spring Boot — declared as `compileOnly` so consumers bring their own
    // version. They pick the Spring Boot version, we just use its APIs.
    compileOnly(libs.spring.boot.starter)
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.boot.web)
    compileOnly(libs.spring.boot.security)
    compileOnly(libs.spring.boot.data.redis)
    compileOnly(libs.spring.boot.validation)
    compileOnly(libs.spring.session.redis)
    compileOnly(libs.jobrunr.spring)

    // JWT validation — bundled because it's our concrete choice
    api(libs.auth0.jwt)
    api(libs.auth0.jwks)

    // Konform — exposed in our public API (Validation<T>, DomainValidationException.errors)
    api(libs.konform)

    // Tests
    testImplementation(libs.spring.boot.starter)
    testImplementation(libs.spring.boot.autoconfigure)
    testImplementation(libs.spring.boot.web)
    testImplementation(libs.spring.boot.security)
    testImplementation(libs.spring.boot.data.redis)
    testImplementation(libs.spring.boot.validation)
    testImplementation(libs.spring.session.redis)
    testImplementation(libs.jobrunr.spring)
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.strikt.core)
    testImplementation(libs.testcontainers.junit)

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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("Kotlin SaaS Starter")
                description.set("Opinionated transversal library for B2B SaaS in Kotlin + Spring Boot")
                url.set("https://github.com/SergioDelgado-tech/kotlin-saas-starter")
                developers {
                    developer {
                        id.set("granchi")
                        name.set("Granchi")
                    }
                }
                scm {
                    url.set("https://github.com/SergioDelgado-tech/kotlin-saas-starter")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/SergioDelgado-tech/kotlin-saas-starter")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR")).getOrElse("")
                password = providers.gradleProperty("gpr.token")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN")).getOrElse("")
            }
        }
    }
}
