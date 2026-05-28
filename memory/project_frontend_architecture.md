---
name: project_frontend_architecture
description: The kotlin-saas-template uses server-rendered Thymeleaf MVC, not a SPA or REST API — affects error handler and response format decisions
metadata:
  type: project
---

The canonical consumer (`kotlin-saas-template`) is a **server-rendered Thymeleaf MVC app**, not a SPA or REST API. Dependencies: `spring-boot-thymeleaf`, `thymeleaf-layout-dialect`. Templates live at `app/src/main/resources/templates/` (dashboard, billing, organization, layout, fragments).

**Why:** this was the chosen architecture; a separate REST API layer has not been decided yet.

**How to apply:** the existing `GlobalExceptionHandler` (returning view names like `"error/422"`) is correct for this consumer. Don't replace it with a `ProblemDetail` / `@RestControllerAdvice` variant speculatively — only when a REST API layer is actually added (tracked in #25, now an enhancement with a decision gate).
