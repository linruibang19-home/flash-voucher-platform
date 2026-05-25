# Flash Voucher Platform

Flash Voucher Platform is a Spring Boot and Vue project being refactored from a monolith into a service-oriented architecture.

## Repository Layout

```text
apps/        Edge applications, currently the API gateway.
services/    Deployable business services.
shared/      Shared DTOs, contracts, and common utilities.
frameworks/  Internal technical framework modules.
frontend/    Vue frontend application.
docs/        Architecture and local verification documents.
sql/         Database schema and seed SQL.
```

## Current Runtime Modules

- `apps/hmdp-gateway`
- `services/hmdp-auth-service`
- `services/hmdp-user-service`
- `services/hmdp-shop-service`
- `services/hmdp-core-service`

See `docs/architecture-refactor-plan.md` and `docs/local-routing-verification.md` for the current split plan and routing matrix.
