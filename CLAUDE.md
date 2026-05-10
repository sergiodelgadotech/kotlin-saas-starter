# CLAUDE.md — kotlin-saas-starter

This file gives Claude Code the context it needs to work effectively on this repository. Read it first.

## Workflow Conventions

- Use trunk-based development; do NOT propose feature branches.

## What this repo is

`kotlin-saas-starter` is an opinionated **library** that captures the transversal infrastructure for B2B SaaS products built in Kotlin + Spring Boot. It is **not an application** — it has no `main()`, no controllers, no domain entities. It exposes reusable building blocks that consuming applications wire into their own setup.

It is published as a Maven artifact to GitHub Packages and consumed by SaaS applications via:

```kotlin
implementation("org.granchi:kotlin-saas-starter:0.1.0")
```

The companion repository `kotlin-saas-template` is the canonical example of how to consume this library.

## The library's scope

What lives here:

- **Multitenancy primitives** — `TenantContext`, `TenantInterceptor`, `TenantResolver` interface
- **JWT authentication** — `JwtAuthFilter` for Zitadel or any OIDC provider
- **Distributed locks** — `RedisLockService` using SET NX PX
- **Rate limiting** — `RateLimiter` and `RateLimitInterceptor` using Redis sorted sets
- **Async jobs** — `JobSchedulerService` and `TenantJobFilter` for tenant-aware Jobrunr execution
- **Domain validation** — Konform helpers and `DomainValidationException`
- **Web error handling** — `GlobalExceptionHandler` base class

What does NOT live here, by design:

- Domain entities (organization, billing, etc.) — these belong in consuming apps
- Repositories with concrete schemas — apps own their persistence
- Controllers — apps own their HTTP surface
- `application.yml`, Flyway migrations, Docker, infra — apps own their configuration and deployment

The principle: **only extract to the starter when a pattern has appeared in at least two real SaaS products**. Premature generalization is worse than duplication.

## Architectural decisions

These are non-negotiable. If you're tempted to change them, propose it as a discussion first.

- **Spring dependencies are `compileOnly`** — consumers bring their own Spring Boot version. The starter uses Spring APIs but doesn't pin its version. This avoids dependency conflicts in consuming apps.
- **No transitive dependencies on application-level code** — the library knows nothing about specific entities like `Member` or `Organization`. It uses interfaces (e.g. `TenantResolver`) that consumers implement.
- **Public API surface is minimized** — internal helpers are `internal`. Anything `public` is a commitment we honor with semantic versioning.
- **Kotlin-first** — no Java compatibility layer, no `JvmStatic` unless strictly necessary. Consumers are assumed to be Kotlin codebases.

## Project structure

### Repo locations

- `kotlin-saas-template` lives at `../kotlin-saas-template` (real local clone). Do NOT clone fresh into `/tmp`.

### Source layout

```
src/main/kotlin/org/granchi/saasstarter/
├── tenant/        TenantContext, TenantInterceptor, TenantResolver
├── security/      JwtAuthFilter
├── lock/          RedisLockService
├── ratelimit/     RateLimiter, RateLimitInterceptor
├── jobs/          JobSchedulerService, TenantJobFilter
├── validation/    Validation extensions, DomainValidationException
└── web/           GlobalExceptionHandler

src/test/kotlin/org/granchi/saasstarter/
└── (mirror structure with unit + integration tests)
```

## How to work in this repo

### Build and test
```bash
./gradlew build               # compiles + runs tests
./gradlew test                # tests only
./gradlew publishToMavenLocal # install locally for testing in kotlin-saas-template
```

### Versioning and releases
This repo uses **release-please** for automated versioning. Every commit to `main` must follow [Conventional Commits](https://www.conventionalcommits.org):

- `fix: ...`            → patch bump (0.1.0 → 0.1.1)
- `feat: ...`           → minor bump (0.1.0 → 0.2.0)
- `feat!: ...` or `BREAKING CHANGE:` in body → major bump (0.1.0 → 1.0.0)
- `chore:`, `docs:`, `test:`, `refactor:` → no version bump but appears in changelog

Release-please opens a release PR in `main` that accumulates changes. When merged, it tags and triggers publish to GitHub Packages.

### Adding new code
Before adding anything new, check that:

1. The pattern has appeared in **at least two** consuming SaaS apps. If only one app needs it, it doesn't belong here yet.
2. It doesn't introduce concrete domain coupling. Use interfaces that consumers implement.
3. It can be tested without spinning up a full Spring Boot app, or it has a clear integration test using Testcontainers.

### When modifying existing code
Anything `public` is a commitment. Breaking changes require a major bump and `feat!:` commit prefix. If unsure whether a change is breaking, ask — don't guess.

## Tests

Test layers in this repo:

- **Unit** — JUnit 5 + Strikt assertions. Fast, no Spring context.
- **Integration** — `@SpringBootTest` slices with Testcontainers for Redis/Postgres when needed.

There are no E2E tests here — those belong in the consuming app's test suite.

## Planning workflow

Implementation plans for this library live as **GitHub issues**, not markdown files. They're tracked alongside template-side issues on a single board:

- **Project board:** <https://github.com/users/serandel/projects/6> ("Starter/template split") — surfaces issues from this repo and `kotlin-saas-template` together. The custom `Plan` field groups items by plan number.
- **Labels:** plan issues carry `plan` and `starter-split`; cross-repo plans also carry `cross-repo` and a `> **Companion:**` blockquote at the top of the body linking the partner issue in the template repo.
- **Sequencing:** each plan issue has a `### Blocked by` task list whose items auto-check when the referenced issues close.

When a new plan is needed, create the issue (or pair of issues for cross-repo work) — don't write a new markdown plan file.

## Things Claude should NOT do without asking

- Adding a new public interface or class — propose it first
- Changing the package structure
- Adding new transitive dependencies — discuss whether they should be `compileOnly` or `api`
- Modifying the publishing setup
- Touching the release-please config

## Things Claude can do freely

- Fixing bugs that have a failing test
- Improving documentation
- Adding tests to existing classes
- Internal refactors that don't change public API
- Adding `internal` helpers used by existing code
