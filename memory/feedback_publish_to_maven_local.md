---
name: Composite build replaces publishToMavenLocal
description: The template uses a Gradle composite build to resolve the starter locally — no publishToMavenLocal needed for cross-repo development
type: feedback
originSessionId: f42991ad-d37e-48b9-939e-a54014347778
---
Use the Gradle composite build for local cross-repo work, not `publishToMavenLocal`. The template's `settings.gradle.kts` already has `includeBuild("../kotlin-saas-starter")` (conditional on the path existing), so Gradle substitutes the local starter source automatically.

**Why:** `publishToMavenLocal` is deprecated as the local dev workflow. The composite build is set up as of the `77de1bc` commit on the template (2026-05-14). The user confirmed this is the correct approach.

**How to apply:**
- When plan steps say "publishToMavenLocal -Pversion=X-SNAPSHOT", skip them — the composite build makes version bumps unnecessary for local testing.
- Don't bump `libs.versions.toml` in the template to a SNAPSHOT version either; the composite build ignores the declared version and uses local source directly.
- Only bump the template's `libs.versions.toml` to a released version (e.g. `0.3.0`) after release-please has published the artifact to GitHub Packages — and only when the user asks to do so.
