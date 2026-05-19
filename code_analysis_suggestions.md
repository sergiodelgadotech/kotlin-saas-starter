# Project Analysis & Improvement Suggestions

This document summarizes the architecture, identified flaws, and recommended improvements for the `kotlin-saas-starter` project.

---

## 1. Project Summary

The **kotlin-saas-starter** is an opinionated library containing transversal infrastructure primitives for B2B SaaS applications built in Kotlin and Spring Boot. It uses a decoupled architecture where consumer applications provide their own domain entities and concrete implementations of configuration properties.

### Key Modules
* **Multitenancy**: Thread-local [TenantContext](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/tenant/TenantContext.kt) and servlet [TenantInterceptor](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/tenant/TenantInterceptor.kt).
* **Security**: JWT authentication filter targeting OIDC provider (Zitadel).
* **Distributed Locks**: [RedisLockService](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/lock/RedisLockService.kt) using Redis SET NX PX.
* **Rate Limiting**: Sliding window [RateLimiter](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/ratelimit/RateLimiter.kt) backed by Redis sorted sets.
* **Background Jobs**: [JobSchedulerService](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/jobs/JobSchedulerService.kt) integration with JobRunr.
* **Validation**: Extensible Konform helpers.
* **Web**: Global exception handler templates.

---

## 2. Identified Flaws & Code Smells

### 🔴 Flaw 1: Lack of JWT Key Caching (Performance/Network Bottleneck)
* **Location**: [JwtAuthFilter.kt](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/security/JwtAuthFilter.kt#L63-L75)
* **Description**: A new `UrlJwkProvider` is instantiated on *every* request. This does not cache the OIDC keys, causing the application to issue an HTTP request to the external identity provider (e.g. Zitadel) to fetch the JWKS on every incoming API request.
* **Impact**: Introduces massive network latency to all authenticated requests and risks hitting IDP rate limits.
* **Solution**: Instantiate `JwkProvider` once (e.g., as a Spring `@Bean` or class property) using `JwkProviderBuilder` configured with caching and rate-limiting.

### 🔴 Flaw 2: Non-Atomic Lock Release (Race Condition)
* **Location**: [RedisLockService.kt](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/lock/RedisLockService.kt#L49-L54)
* **Description**: The lock release reads the value and deletes the key in two separate non-atomic steps:
  ```kotlin
  val currentValue = redisTemplate.opsForValue().get(lockKey)
  if (currentValue == lockValue) {
      redisTemplate.delete(lockKey)
  }
  ```
* **Impact**: If a thread is suspended after the `get` check but before the `delete`, or the lock expires naturally and is acquired by another process, the original thread will mistakenly delete the new lock owned by the other process.
* **Solution**: Execute the release using an atomic Redis Lua script.

### 🟡 Flaw 3: Flawed Rate Limiter Sliding Window Logic
* **Location**: [RateLimiter.kt](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/ratelimit/RateLimiter.kt#L27-L42)
* **Description**:
  1. The sliding window unconditionally appends the current timestamp via `ops.add()` even when the client is already rate-limited. This causes successive requests from a blocked client to continuously push the block forward, locking them out indefinitely.
  2. If multiple requests arrive in the exact same millisecond, they share the same member value (`now.toString()`). In Redis sorted sets, member names are unique, so subsequent requests overwrite the previous ones instead of incrementing the count.
* **Solution**: Append a unique request suffix (e.g., `"$now:${UUID.randomUUID()}"`) to differentiate requests, and only append timestamps when requests are successful.

### 🟡 Flaw 4: Missing Auto-Configurations
* **Location**: [AutoConfiguration.imports](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports)
* **Description**: Core components like `TenantInterceptor`, `JwtAuthFilter`, `RedisLockService`, `RateLimiter`, and `GlobalExceptionHandler` are not registered in the autoconfig imports list. Consuming applications have to manually add `@ComponentScan` or bean definitions.
* **Solution**: Group modules into modular auto-configuration classes (e.g., `TenantAutoConfiguration`, `SecurityAutoConfiguration`, `RateLimitAutoConfiguration`, `LockAutoConfiguration`) and list them under the autoconfigure imports file.

### 🟡 Flaw 5: Global/Hardcoded Rate Limit Interceptor
* **Location**: [RateLimitInterceptor.kt](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/ratelimit/RateLimitInterceptor.kt#L22)
* **Description**: The rate limit interceptor hardcodes a flat limit of `100 requests per minute` and applies it globally to all intercepted URIs. Consuming applications cannot configure custom limits, disable it for public routes, or adjust limits based on tenant plan tiers.
* **Solution**: Implement configuration properties or support custom annotations (e.g., `@RateLimit(limit = 50, window = 30)`) on controllers.

### 🔵 Flaw 6: Global Exception Handler Assumes MVC Views
* **Location**: [GlobalExceptionHandler.kt](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/web/GlobalExceptionHandler.kt#L23)
* **Description**: The exception handler returns view names (e.g., `"error/422"`), assuming server-rendered templates (like Thymeleaf). This fails when a consumer implements a REST API/SPA architecture.
* **Solution**: Return Spring's standard RFC 7807 `ProblemDetail` or custom JSON response models, or provide separate REST exception handler templates.

### 🔵 Flaw 7: ThreadLocal Context in Coroutines/Virtual Threads
* **Location**: [TenantContext.kt](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/tenant/TenantContext.kt#L6)
* **Description**: Using raw JVM `ThreadLocal` works fine for standard thread-per-request applications, but breaks context propagation when consuming applications run async logic, Coroutines, or Spring WebFlux.
* **Solution**: Expose Coroutines context extensions (e.g. using `asContextElement()`) to propagate tenant IDs safely across coroutines.

---

## 3. Best Next Actions

1. **Add Logic/Integration Tests**: Write unit and integration tests (with testcontainers/mocking) for the core infrastructure features to prevent regressions (the current suite has zero tests for core services).
2. **Implement JWKS Caching**: Refactor [JwtAuthFilter.kt](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/security/JwtAuthFilter.kt) to reuse a cached and rate-limited `JwkProvider`.
3. **Secure the Lock Release**: Refactor [RedisLockService.kt](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/lock/RedisLockService.kt) to execute the release check-and-delete via a Redis Lua script.
4. **Improve the Sliding Window**: Refactor [RateLimiter.kt](file:///var/home/serandel/Projects/kotlin-saas-starter/src/main/kotlin/org/granchi/saasstarter/ratelimit/RateLimiter.kt) to resolve timestamp collisions and prevent spammed-request lockouts.
