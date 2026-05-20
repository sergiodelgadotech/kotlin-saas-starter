---
name: feedback-autoconfig-inner-class-pattern
description: "Spring Boot autoconfiguration pattern for starter beans that need a RedisConnectionFactory — inner-class @ConditionalOnBean fails, correct fix is to drop the condition and use a unique bean name"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: cc076e5e-e0b2-4277-b0c5-86b3f0e9a246
---

`@ConditionalOnBean(RedisConnectionFactory::class)` on an inner `@Configuration` class inside an `@AutoConfiguration` does NOT work even with `@AutoConfigureAfter`. The condition is evaluated before the factory bean definition is registered by Spring Boot's `RedisAutoConfiguration`, so it always returns false. This is a pre-existing issue that also affects `SessionAutoConfiguration.EnableRedisSessionConfig`.

**Why:** Spring's conditions on inner `@Configuration` member classes are evaluated in the same processing pass as the outer class, and `@AutoConfigureAfter` ordering does not guarantee that the dependency's beans are registered by that point.

**How to apply:** When writing a new `@AutoConfiguration` in the starter that needs `RedisConnectionFactory`:

1. Drop `@ConditionalOnBean(RedisConnectionFactory::class)` from the inner class entirely. The outer class's `@ConditionalOnClass(RedisConnectionFactory::class)` is sufficient to gate on the class being on the classpath.

2. If the autoconfig registers a `RedisTemplate`, use a **unique bean name** (e.g. `@Bean("jsonRedisTemplate")`) to avoid a naming conflict with Spring Boot's own `redisTemplate` bean (`RedisTemplate<Object, Object>`). Spring Boot's version is registered first (it runs before ours); registering a second bean named `redisTemplate` causes `BeanDefinitionOverrideException`. With a unique name, `RateLimiter` and similar services find the correct bean by type (`RedisTemplate<String, Any>`) since the two templates have different generic signatures that Spring resolves via `ResolvableType`.

3. Use `@ConditionalOnMissingBean` (no params, relies on implicit return-type matching) so consumers can override with their own `RedisTemplate<String, Any>`.

The `RedisConnectionFactory` parameter in the `@Bean` method is injected at **instantiation** time (not definition-registration time), so it is available by then. See `RedisAutoConfiguration.BeansConfig` for the canonical implementation.

Related: [[feedback-publish-to-maven-local]], [[project-frontend-architecture]]
