# hmdp-api

This module holds API contracts for future service boundaries.

The initial submodules are intentionally lightweight. They establish stable Maven boundaries before runtime services are split out of `hmdp-core-service`.

Current API domains:

- `hmdp-api-auth`
- `hmdp-api-user`
- `hmdp-api-shop`
- `hmdp-api-blog`
- `hmdp-api-voucher`
- `hmdp-api-order`
- `hmdp-api-notify`

Guidelines:

- Keep API modules free of persistence entities and mapper dependencies.
- Prefer DTOs from `hmdp-parameter`.
- Add Feign clients or request/response contracts here only when the corresponding service split starts.

