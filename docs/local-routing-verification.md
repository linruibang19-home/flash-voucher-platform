# Local Routing Verification

This document tracks the temporary routing setup while the monolith is being split into services.

## Required Services

Start the services in separate terminals:

```bash
mvn -pl :hmdp-auth-service spring-boot:run
mvn -pl :hmdp-user-service spring-boot:run
mvn -pl :hmdp-shop-service spring-boot:run
mvn -pl :hmdp-blog-service spring-boot:run
mvn -pl :hmdp-core-service spring-boot:run
mvn -pl :hmdp-gateway spring-boot:run
```

Default ports:

- Gateway: `8080`
- Auth Service: `8081`
- User Service: `8082`
- Shop Service: `8083`
- Blog Service: `8084`
- Core Service: `8085`

## Current Gateway Routes

Auth Service:

- `POST /user/code`
- `POST /user/login`
- `POST /user/logout`
- `GET /user/me`

User Service:

- `GET /user/{id}`
- `GET /user/info/{id}`
- `POST /user/level/update`
- `POST /user/sign`
- `GET /user/sign/count`

Shop Service:

- `/shop/**`
- `/shop-type/**`

Blog Service:

- `/blog/**`
- `/follow/**`
- `/blog-comments/**`

Core Service:

- `/voucher/**`
- `/voucher-order/**`
- `/upload/**`
- `/test/**`

There is intentionally no `/user/**`, `/shop/**`, `/shop-type/**`, `/blog/**`, or `/follow/**` fallback route to Core Service. Missing extracted routes should fail fast instead of silently using the old monolith endpoint.

## Smoke Test Flow

1. Request a code:

```bash
curl -X POST "http://localhost:8080/user/code?phone=13838411438"
```

2. Login with the returned code:

```bash
curl -X POST "http://localhost:8080/user/login" \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"13838411438\",\"code\":\"123456\"}"
```

3. Use the returned token:

```bash
curl "http://localhost:8080/user/me" -H "authorization: <token>"
curl "http://localhost:8080/user/1" -H "authorization: <token>"
curl -X POST "http://localhost:8080/user/sign" -H "authorization: <token>"
curl "http://localhost:8080/user/sign/count" -H "authorization: <token>"
curl "http://localhost:8080/shop-type/list"
curl "http://localhost:8080/shop/1"
curl "http://localhost:8080/shop/of/type?typeId=1&current=1"
```

Gateway should add `X-Request-Id` and `X-User-Id` to downstream requests after authentication.
