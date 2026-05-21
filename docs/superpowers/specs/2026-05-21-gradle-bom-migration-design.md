# Design: Migrate to Gradle-native BOM support

**Date:** 2026-05-21
**Issues:** [kotlin-saas-starter#27](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/27) · [kotlin-saas-template#18](https://github.com/SergioDelgado-tech/kotlin-saas-template/issues/18)

## Problem

Running `./gradlew build --warning-mode all` on Gradle 9+ emits two deprecation warnings that will become hard errors in Gradle 10:

```
Declaring a Usage attribute with a legacy value has been deprecated.
A Usage attribute was declared with value 'java-api-jars'.
A Usage attribute was declared with value 'java-runtime-jars'.
```

Both originate from the `io.spring.dependency-management` plugin (v1.1.7, last updated December 2024). The plugin uses Gradle internals that were deprecated in Gradle 9 and will be removed in Gradle 10.

## Fix

### kotlin-saas-starter#27

Replace the `io.spring.dependency-management` plugin with Gradle's native BOM support.

**`gradle/libs.versions.toml`:**
- Add `spring-boot-bom = { module = "org.springframework.boot:spring-boot-dependencies", version.ref = "spring-boot" }` to `[libraries]`
- Remove `spring-dep = "1.1.7"` from `[versions]`
- Remove `spring-dep = { id = "io.spring.dependency-management", version.ref = "spring-dep" }` from `[plugins]`

**`build.gradle.kts`:**
- Remove `alias(libs.plugins.spring.dep)` from the `plugins` block
- Delete the entire `dependencyManagement { imports { mavenBom(...) } }` block
- Add `compileOnly(platform(libs.spring.boot.bom))` at the top of the `compileOnly` Spring Boot group
- Add `testImplementation(platform(libs.spring.boot.bom))` at the top of the `testImplementation` Spring Boot group

**Verification:** `./gradlew build --warning-mode all` — no `java-api-jars` or `java-runtime-jars` deprecation warnings.

### kotlin-saas-template#18 (companion)

The template's `app` subproject registers multiple `Test` tasks without explicitly declaring the JUnit Platform Launcher, causing a similar Gradle deprecation warning.

**`gradle/libs.versions.toml`** (if not present): add `junit-platform-launcher` library entry (version managed by Spring Boot BOM).

**`app/build.gradle.kts`:** add `testRuntimeOnly(libs.junit.platform.launcher)` to the `dependencies` block.

**Verification:** `./gradlew :app:build --warning-mode all` — no JUnit launcher deprecation warning.

## Out of scope

- No version bumps to Spring Boot or any other dependency.
- No changes to how the starter is consumed by the template.
