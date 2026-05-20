# Design: Fix SessionAutoConfiguration.EnableRedisSessionConfig conditional

**Issue:** kotlin-saas-starter#28  
**Date:** 2026-05-20

## Problem

`SessionAutoConfiguration.EnableRedisSessionConfig` has `@ConditionalOnBean(RedisConnectionFactory::class)` at the class level. Spring evaluates this condition during bean-definition registration — before Spring Boot's `RedisAutoConfiguration` has registered the factory, even with `@AutoConfigureAfter`. The inner class never activates, `@EnableRedisHttpSession` is never applied, and HTTP sessions are never stored in Redis.

This is the same root-cause bug fixed in `RedisAutoConfiguration.BeansConfig` (commit `36ece66`) and documented in project memory (`feedback-autoconfig-inner-class-pattern`).

## Fix

**File:** `src/main/kotlin/org/granchi/saasstarter/autoconfigure/SessionAutoConfiguration.kt`

1. Remove `@ConditionalOnBean(RedisConnectionFactory::class)` from `EnableRedisSessionConfig`.
2. Remove the now-unused `ConditionalOnBean` and `RedisConnectionFactory` imports.
3. Update the KDoc — remove the stale sentence that says `@AutoConfigureAfter` makes `RedisConnectionFactory` available at condition-evaluation time (it doesn't; that's the bug).

The outer class's existing `@ConditionalOnClass(EnableRedisHttpSession::class)` and `@ConditionalOnMissingBean(SessionRepository::class)` are sufficient guards. The `RedisConnectionFactory` is injected naturally at bean instantiation time, not condition-evaluation time.

## Validation

Run the integration smoke test in `kotlin-saas-template`:

```
./gradlew :app:integrationTest --tests "org.granchi.saastemplate.integration.SessionAutoConfigSmokeTest"
```

The `Spring Session provides a SessionRepository bean` test must pass. No changes to starter-side unit tests — `@EnableRedisHttpSession` requires real Redis infrastructure and the smoke test already covers this at the right level.
