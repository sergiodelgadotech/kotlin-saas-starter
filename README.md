# kotlin-saas-starter

[![CI](https://github.com/sergiodelgadotech/kotlin-saas-starter/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/sergiodelgadotech/kotlin-saas-starter/actions/workflows/ci.yml) [![stable](https://img.shields.io/github/v/release/sergiodelgadotech/kotlin-saas-starter?label=stable&color=blue)](https://github.com/sergiodelgadotech/kotlin-saas-starter/releases/latest) [![snapshot](https://img.shields.io/badge/dynamic/regex?url=https%3A%2F%2Fraw.githubusercontent.com%2Fsergiodelgadotech%2Fkotlin-saas-starter%2Frelease-please--branches--main--components--kotlin-saas-starter%2Fgradle.properties&search=version%3D%28.*%29&replace=%241-SNAPSHOT&label=snapshot&color=orange)](https://github.com/orgs/sergiodelgadotech/packages?repo_name=kotlin-saas-starter) [![License: LGPL v2.1](https://img.shields.io/badge/License-LGPL_v2.1-blue.svg)](LICENSE)
<!-- Snapshot badge reads gradle.properties from the release-please PR branch
     (release-please--branches--main--components--kotlin-saas-starter). If that branch
     doesn't exist (brief window after a release before the next conventional commit),
     the badge renders an error. Update the URL if release-please's branch naming changes. -->

Opinionated transversal library for B2B SaaS products built in Kotlin + Spring Boot.

This library captures the infrastructure code that every SaaS repeats — multitenancy, auth, distributed locks, rate limiting, async jobs, validation — so that consuming applications can focus on their domain.

## Quick start

```kotlin
// build.gradle.kts of your SaaS application
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/granchi/kotlin-saas-starter")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.token").orNull
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("org.granchi:kotlin-saas-starter:0.1.0")
}
```

## What's inside

| Package      | Purpose |
|--------------|---------|
| `tenant`        | `TenantContext` (thread-local), `TenantInterceptor`, `TenantResolver` interface |
| `security`      | `JwtAuthFilter` validates JWTs from any OIDC provider (Zitadel, Keycloak, etc.) |
| `lock`          | `RedisLockService` for distributed locks — atomic release via `DELEX IFEQ` (Redis 8.4+) |
| `ratelimit`     | Sliding window rate limiter on Redis sorted sets |
| `jobs`          | Tenant-aware Jobrunr execution — automatic context propagation |
| `validation`    | Konform helpers + `DomainValidationException` |
| `web`           | `GlobalExceptionHandler` base for consistent error responses |
| `observability` | Correlation ID propagation, tenant MDC, and Micrometer observation tagging |

## Wiring it into your app

The library uses interfaces for anything app-specific. You implement them:

```kotlin
@Component
class MyTenantResolver(
    private val memberRepository: MemberRepository
) : TenantResolver {
    override fun resolveTenantId(userId: String): UUID? =
        memberRepository.findOrganizationIdByUserId(userId)
}
```

Then register the interceptors in your Spring config:

```kotlin
@Configuration
class WebMvcConfig(
    private val tenantInterceptor: TenantInterceptor
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(tenantInterceptor)
            .addPathPatterns("/app/**")
    }
}
```

See the companion repo `mvp-saas-template` for a full example.

## Observability

`ObservabilityAutoConfiguration` registers three components automatically on any servlet web application:

### MDC keys

| Key              | Set by                  | Scope |
|------------------|-------------------------|-------|
| `correlation_id` | `CorrelationIdFilter`   | All requests — present from the very first filter in the chain |
| `tenant_id`      | `TenantMdcInterceptor`  | Controller and downstream — set after the interceptor chain runs |

**Filter vs. interceptor asymmetry:** `correlation_id` is set by a servlet filter at `Ordered.HIGHEST_PRECEDENCE`, so it appears in every log line including security and other early filters. `tenant_id` is set by a `HandlerInterceptor`, which runs after the filter chain — it is available in controller logs and any beans called from controllers, but NOT in filter-time logs. This is an intentional trade-off: the tenant isn't resolved until `TenantInterceptor` runs (which itself is an interceptor), so an earlier MDC entry for `tenant_id` would always be empty.

The `X-Correlation-Id` header is echoed back on every response so clients and upstreams can correlate requests across services.

### Micrometer observation tagging

`TenantObservationFilter` adds `tenant.id` as a **low-cardinality key-value** on every Micrometer `Observation`. It is only registered when an `ObservationRegistry` bean is present in the application context (e.g. when `spring-boot-starter-actuator` is on the classpath).

**Cardinality warning:** Low cardinality is appropriate for B2B SaaS products with hundreds or thousands of tenants. If your product uses a consumer-scale model where tenants map 1:1 to end-users (millions), the `tenant.id` tag will cause metric label explosion in your metrics backend. In that case, override `TenantObservationFilter` and use `highCardinalityKeyValue` instead:

```kotlin
@Bean("saasStarterTenantObservationFilter")
fun tenantObservationFilter() = ObservationFilter { context ->
    if (TenantContext.isPresent()) {
        context.addHighCardinalityKeyValue(
            KeyValue.of("tenant.id", TenantContext.get().toString())
        )
    }
    context
}
```

## Project tracking

Active development is tracked on the [Starter/template split project](https://github.com/users/serandel/projects/6), which spans this library and the [mvp-saas-template](https://github.com/serandel/mvp-saas-template) reference application.

## Stack opinions

This library is opinionated. It assumes:

- **Kotlin + Spring Boot 3+**
- **PostgreSQL** as primary database (via Flyway in your app)
- **Redis 8.4+** for caching, locks, sessions, rate limiting (lock release uses the native `DELEX IFEQ` command added in 8.4)
- **Zitadel or any OIDC provider** for authentication
- **Jobrunr** for async jobs

If your stack diverges from these, this library is probably not for you.

## Third-party notices

`NOTICE` at the repo root lists all bundled runtime dependencies and their licenses.
It is regenerated automatically on every `./gradlew build`.
If you update a dependency version, commit the updated `NOTICE` alongside the version bump.

## Versioning

Follows [Semantic Versioning](https://semver.org). Releases are automated via [release-please](https://github.com/googleapis/release-please) — see `CONTRIBUTING.md`.

