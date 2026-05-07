# Contributing

## Commit message format

This repo uses [Conventional Commits](https://www.conventionalcommits.org). Every commit must follow:

```
<type>: <description>

[optional body]

[optional footer]
```

Types that affect versioning:

| Type    | Effect on version | Example |
|---------|-------------------|---------|
| `feat`  | minor (0.1.0 → 0.2.0) | `feat: add Redis Streams support` |
| `fix`   | patch (0.1.0 → 0.1.1) | `fix: tenant context leak in async jobs` |
| `feat!` | major (0.1.0 → 1.0.0) | `feat!: rename TenantContext.get() to current()` |

Types that don't affect version (but appear in changelog):

- `docs:` — documentation
- `refactor:` — code change without behavior change
- `test:` — adding tests
- `chore:` — tooling, dependencies
- `ci:` — CI/CD changes

## Breaking changes

Any commit with `!` after the type, OR with a `BREAKING CHANGE:` footer, triggers a major version bump:

```
feat!: change TenantResolver signature to return Optional<UUID>

BREAKING CHANGE: TenantResolver.resolveTenantId now returns Optional<UUID>
instead of UUID? to align with Java consumers.
```

## Release flow

1. Push commits to `main` following Conventional Commits
2. release-please opens a PR with the next version + changelog
3. The PR accumulates changes until you merge it
4. Merging the release PR creates a Git tag and triggers publish to GitHub Packages

You don't manually create tags or edit the changelog.

## Local development

```bash
./gradlew build                 # compile + test
./gradlew publishToMavenLocal   # install in ~/.m2 for local testing in mvp-saas-template
```

To test against `mvp-saas-template` locally without publishing:

```kotlin
// In mvp-saas-template's build.gradle.kts
repositories {
    mavenLocal()  // before mavenCentral
    // ...
}
```
