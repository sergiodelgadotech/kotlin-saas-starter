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

## Dev container (VS Code, Cursor, JetBrains Gateway)

A `.devcontainer/devcontainer.json` is included so you can develop inside a pre-configured container with JDK 25, Gradle, Docker socket access, and the Claude Code CLI — no manual setup required.

### Prerequisites

- **Docker Desktop** (macOS / Windows) or **Docker Engine** (Linux)
- **VS Code** with the [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) extension, **Cursor**, or **JetBrains Gateway**
- `GITHUB_ACTOR` and `GITHUB_TOKEN` (with `read:packages` scope) set in your shell profile — the container inherits them to authenticate against GitHub Packages

### Opening the container

**VS Code / Cursor:** open the repo folder, then run **Dev Containers: Reopen in Container** from the command palette.

**JetBrains Gateway:** choose **Connect to Dev Container** and select the repo folder.

### Notes

- **`remoteUser` is `vscode`** — this is the image's built-in non-root user, unrelated to the VS Code editor. It works identically with JetBrains Gateway.
- **Claude Code** is available in the integrated terminal. Run `claude` once to authenticate. Auth state is persisted across container rebuilds via a named Docker volume.
- **`./gradlew test` works inside the container** — Testcontainers launches Postgres by reaching the host Docker daemon through the mounted socket (`docker-outside-of-docker` feature). No extra Docker Compose setup is needed.
- **No Spring profile needed** — this is a library; tests are self-contained.
