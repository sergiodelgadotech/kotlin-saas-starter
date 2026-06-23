---
name: project-observability-plan
description: Plan 1 on the project board — observability foundations for starter and template; Grafana Cloud chosen as backend
metadata: 
  node_type: memory
  type: project
  originSessionId: 8b5195e2-fee0-4334-949e-6fb9f17a00a3
---

**COMPLETED 2026-05-31** — All four issues merged to main on both repos.

Plan 1 = observability foundations. Four issues, all closed:

- **Starter #49** (Issue A): `CorrelationIdFilter`, `TenantMdcInterceptor`, `TenantObservationFilter`, `ObservabilityAutoConfiguration`. MDC keys: `correlation_id` (filter), `tenant_id` (interceptor). Micrometer `tenant.id` low-cardinality key on all observations.
- **Starter #50** (Issue B): `saasstarter.lock`, `saasstarter.ratelimit`, `saasstarter.auth.jwt`, `saasstarter.job`, `saasstarter.webhook.stripe` observations on all five operational classes. Note: `bucket` key in RateLimiter is `highCardinalityKeyValue` (raw Redis key contains IP/user data).
- **Starter #51** (Issue C): docs-only — Jobrunr 8 ships `JobRunrHealthIndicator` built-in; Spring Boot covers Redis via `RedisHealthIndicator`.
- **Template #49** (Issue D): Prometheus registry, OTel tracing bridge, OTLP exporter, Logstash JSON encoder wired. `OTEL_EXPORTER_OTLP_ENDPOINT` env var targets Grafana Cloud. `/actuator/prometheus`, `/actuator/info`, `/actuator/metrics` permitted without auth (restrict via reverse proxy in production).

**Why:** Grafana Cloud free tier chosen as backend (10k series, 50GB logs/traces, 14-day retention, forever-free). OTLP exporter is backend-agnostic via env var.

**How to apply:** Plan is complete — no further work needed for baseline observability. Next work would be Grafana Cloud account setup (env var config only, no code changes) or custom dashboards.
