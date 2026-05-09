---
name: Publishing kotlin-saas-starter to mavenLocal
description: When a build chain needs an updated starter artifact (typically the template consuming this library), run publishToMavenLocal here without asking
type: feedback
---
The template (`../kotlin-saas-template`) consumes `org.granchi:kotlin-saas-starter` from `mavenLocal()` (added to its `settings.gradle.kts` as a fallback alongside GitHub Packages). When a build needs a starter version that isn't in `~/.m2/repository/org/granchi/kotlin-saas-starter/<version>/` yet, run `./gradlew publishToMavenLocal` in this repo directly — no need to confirm.

**Why:** the starter isn't on GitHub Packages yet. Until it is, `publishToMavenLocal` is the bridge. The user explicitly authorized this on 2026-05-08 ("feel free to publish to maven local in the sibling repo yourself when needed").

**How to apply:**
- Trigger on Gradle errors like `Could not find org.granchi:kotlin-saas-starter:<v>` when building the template against a fresh starter version.
- After publishing, re-run the failing template task.
- This authorization is for `publishToMavenLocal` only — don't push the starter to GitHub Packages or run other release tasks (release-please, publishing to remote) without asking.
