# kotlin-saas-starter

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
| `tenant`     | `TenantContext` (thread-local), `TenantInterceptor`, `TenantResolver` interface |
| `security`   | `JwtAuthFilter` validates JWTs from any OIDC provider (Zitadel, Keycloak, etc.) |
| `lock`       | `RedisLockService` for distributed locks via SET NX PX |
| `ratelimit`  | Sliding window rate limiter on Redis sorted sets |
| `jobs`       | Tenant-aware Jobrunr execution — automatic context propagation |
| `validation` | Konform helpers + `DomainValidationException` |
| `web`        | `GlobalExceptionHandler` base for consistent error responses |

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

## Stack opinions

This library is opinionated. It assumes:

- **Kotlin + Spring Boot 3+**
- **PostgreSQL** as primary database (via Flyway in your app)
- **Redis** for caching, locks, sessions, rate limiting
- **Zitadel or any OIDC provider** for authentication
- **Jobrunr** for async jobs

If your stack diverges from these, this library is probably not for you.

## Versioning

Follows [Semantic Versioning](https://semver.org). Releases are automated via [release-please](https://github.com/googleapis/release-please) — see `CONTRIBUTING.md`.

## License

Apache License 2.0 — see `LICENSE`.
