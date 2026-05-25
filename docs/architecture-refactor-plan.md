# Architecture Refactor Plan

This project is being refactored from a single Spring Boot application into a clearer service-oriented architecture. The current priority is to keep the business behavior stable while removing unnecessary complexity.

## Current Baseline

- The ShardingSphere module has been removed.
- The application now uses a single MySQL database named `hmdp`.
- Voucher orders are stored directly in `tb_voucher_order`.
- The order router table and related code have been removed.
- Gateway now provides static routing, request id propagation, access logs, and Redis-backed token checks.
- Auth Service now owns the first extracted login-code and login-token issuing endpoints.
- User Service now owns profile lookup, user info lookup, member-level update, and sign-in endpoints.
- Shop Service now owns shop, shop-type, cache-aside, empty-value cache, Bloom filter warmup, and GEO index warmup.
- Blog Service now owns blog, follow, like, and feed endpoints.
- Core Service no longer exposes `/user`, `/shop`, `/shop-type`, `/blog`, or `/follow` endpoints.
- Core Service now prefers Gateway-propagated `X-User-Id` for user context and keeps Redis token lookup as a compatibility fallback.
- Redis, Kafka, Redisson, Lua-based stock deduction, idempotency, and reconciliation remain part of the core design.

## Target Architecture

```text
hmdp-plus-cloud
├─ apps
│  └─ hmdp-gateway
├─ services
│  ├─ hmdp-auth-service
│  ├─ hmdp-user-service
│  ├─ hmdp-shop-service
│  ├─ hmdp-core-service
│  ├─ hmdp-blog-service
│  ├─ hmdp-voucher-service
│  ├─ hmdp-order-service
│  ├─ hmdp-notify-service
│  └─ hmdp-job-service
├─ shared
│  ├─ hmdp-common
│  ├─ hmdp-parameter
│  └─ hmdp-api
├─ frameworks
│  ├─ hmdp-redis-tool-framework
│  ├─ hmdp-redisson-framework
│  ├─ hmdp-mq-framework
│  └─ hmdp-id-generator-framework
├─ frontend
│  └─ hmdp-vue3
├─ docs
└─ sql
```

## Service Boundaries

### Gateway

- Route all frontend traffic.
- Validate tokens and pass user context through request headers.
- Apply coarse IP/API rate limits.
- Attach trace ids to incoming requests.

### Auth Service

- Send and verify login codes.
- Issue and refresh login tokens.
- Own login-session persistence in Redis.

### User Service

- Own user profile, phone, sign-in, and member-level data.
- Provide member-level query APIs used by voucher rules.

### Shop Service

- Own shop and shop-type data.
- Keep Redis cache, empty-value cache, Bloom filter warmup, and GEO query logic.

### Blog Service

- Own blogs, comments, likes, follows, and feed behavior.
- Read `X-User-Id` propagated by Gateway for authenticated actions.

### Voucher Service

- Own voucher and seckill-voucher data.
- Issue seckill access tokens.
- Apply seckill rate limits.
- Execute Redis Lua stock deduction.
- Publish order creation events.
- Own Redis stock rollback and subscription sets.

### Order Service

- Consume order creation events.
- Create voucher orders idempotently.
- Query and cancel orders.
- Write reconciliation logs.
- Publish order lifecycle events.

### Notify Service

- Send voucher reminder notifications.
- Send auto-issue notifications.
- Handle delayed reminder tasks.

### Job Service

- Run reconciliation tasks.
- Warm caches.
- Scan and compensate abnormal orders.
- Repair Redis stock when reconciliation detects inconsistency.

## Refactor Steps

1. Stabilize the single-database monolith baseline.
2. Clean module dependencies and move shared contracts into common/API modules.
3. Introduce service registry and externalized configuration.
4. Add Gateway and move authentication to Auth Service.
5. Split User Service and expose member-level APIs.
6. Split Shop Service because it is mostly query-oriented and low risk.
7. Split Blog Service.
8. Split Voucher Service while preserving Redis Lua deduction behavior.
9. Split Order Service and move Kafka order creation consumption into it.
10. Split Notify and Job services.
11. Add integration tests for seckill, cancellation, rollback, and reconciliation.
12. Add observability: trace ids, metrics, and consumer failure dashboards.

## Non-Goals

- Do not reintroduce database sharding unless real write volume requires it.
- Do not split every small framework module into a deployable service.
- Do not move cross-service writes into synchronous distributed transactions; prefer Kafka events, idempotency, and reconciliation.

## Immediate Next Blocks

1. Verify Gateway-to-Auth, Gateway-to-User, and Gateway-to-Shop traffic locally.
2. Tighten Core Service dependencies after Blog Service extraction.
3. Split Voucher Service while preserving Redis Lua deduction behavior.

See `docs/local-routing-verification.md` for the current local routing matrix.
