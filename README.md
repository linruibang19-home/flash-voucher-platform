# Flash Voucher Platform

> 作者：lrb

Flash Voucher Platform 是一个面向本地生活场景的**优惠券秒杀平台**。  
后端基于 **Spring Boot 3.5 / Spring Cloud 2025 / MyBatis-Plus**，采用完整微服务架构，配备 Eureka 服务注册与发现、Spring Cloud Gateway 统一网关、Kafka 异步通信、Redis 分布式缓存与分布式锁、Resilience4j 熔断降级，以及 Prometheus + Grafana 可观测性套件，支持一键 Docker Compose 部署。

---

## 技术栈

| 分类 | 技术选型 |
| --- | --- |
| 核心框架 | Spring Boot 3.5、Spring Cloud 2025 |
| 服务网关 | Spring Cloud Gateway |
| 服务注册与发现 | Spring Cloud Netflix Eureka |
| 服务间通信 | Spring Cloud OpenFeign + LoadBalancer |
| 熔断降级 | Resilience4j |
| 异步消息 | Apache Kafka 3.7（KRaft 模式） |
| 缓存与分布式锁 | Redis 7.2 + Redisson |
| 持久层 | MySQL 8.0 + MyBatis-Plus |
| 可观测性 | Micrometer + Prometheus + Grafana |
| 容器化 | Docker + Docker Compose |

---

## 业务架构

```text
用户 / Vue3 前端
        │
        ▼
hmdp-gateway  (8080)       ← 统一鉴权 + 路由转发
        │
        ├──► hmdp-auth-service    (8081)  验证码、登录、登出
        ├──► hmdp-user-service    (8082)  用户资料、签到
        ├──► hmdp-shop-service    (8083)  店铺、地理查询
        ├──► hmdp-blog-service    (8084)  探店笔记、关注、Feed 流
        ├──► hmdp-voucher-service (8085)  优惠券、秒杀入口、库存预热
        ├──► hmdp-order-service   (8086)  异步下单、回滚、对账
        ├──► hmdp-notify-service  (8087)  告警、通知
        └──► hmdp-job-service     (8088)  定时对账调度

服务注册：所有业务服务 → hmdp-registry (8761) [Eureka Server]
服务间调用：Feign + LoadBalancer（via Eureka 发现，Resilience4j 熔断保护）
异步通信：voucher-service ──Kafka──► order-service ──Feign──► notify-service
```

### 核心秒杀链路

```text
用户请求
  → Gateway 校验 Token（Redis Session）并注入 X-User-Id 头
  → Voucher Service：Lua 脚本原子扣减 Redis 库存，投递 Kafka 消息
  → Order Service：消费 Kafka，创建券订单（MySQL 事务）
  → 失败时：库存回滚 + 失败日志 → Feign 调用 Notify Service 告警
  → Job Service：定时扫描 PENDING 订单，触发对账
```

---

## 代码架构

```text
hmdp-plus
├── apps
│   ├── hmdp-gateway            ← Spring Cloud Gateway 网关
│   └── hmdp-registry           ← Eureka Server 注册中心
│
├── services
│   ├── hmdp-auth-service
│   ├── hmdp-user-service
│   ├── hmdp-shop-service
│   ├── hmdp-blog-service
│   ├── hmdp-voucher-service
│   ├── hmdp-order-service
│   ├── hmdp-notify-service
│   └── hmdp-job-service
│
├── shared
│   ├── hmdp-api                ← 各服务 Feign 客户端契约
│   │   ├── hmdp-api-user
│   │   ├── hmdp-api-voucher
│   │   ├── hmdp-api-order
│   │   └── hmdp-api-notify
│   ├── hmdp-common
│   └── hmdp-parameter
│
├── frameworks                  ← 自研 Starter 组件
│   ├── hmdp-redisson-framework         分布式锁
│   ├── hmdp-redis-tool-framework       限流 + 幂等 + 延迟队列
│   ├── hmdp-id-generator-framework     分布式 ID
│   └── hmdp-mq-framework               Kafka 封装
│
├── infra
│   ├── docker-compose.yml
│   └── prometheus.yml
│
├── sql                         ← 数据库初始化脚本
└── pom.xml
```

---

## 服务端口

| 服务 | 端口 | 职责 |
| --- | --- | --- |
| hmdp-gateway | 8080 | 统一入口、Token 鉴权、路由转发 |
| hmdp-auth-service | 8081 | 登录、登出、验证码 |
| hmdp-user-service | 8082 | 用户资料、签到 |
| hmdp-shop-service | 8083 | 店铺、地理位置查询 |
| hmdp-blog-service | 8084 | 探店笔记、关注、Feed 流 |
| hmdp-voucher-service | 8085 | 优惠券、秒杀入口、库存预热 |
| hmdp-order-service | 8086 | 异步下单、回滚、对账 |
| hmdp-notify-service | 8087 | 告警通知 |
| hmdp-job-service | 8088 | 定时对账调度 |
| hmdp-registry | 8761 | Eureka 注册中心 |
| Prometheus | 9090 | 指标采集 |
| Grafana | 3000 | 监控面板（默认密码：admin） |

---

## Gateway 路由规则

```text
POST /user/code, /user/login, /user/logout, /user/me
  → hmdp-auth-service（白名单，无需鉴权）

GET/POST /user/info/**, /user/sign**, /user/{id}
  → hmdp-user-service

GET /shop/**, /shop-type/**
  → hmdp-shop-service

GET/POST /blog/**, /follow/**, /blog-comments/**
  → hmdp-blog-service

GET/POST /voucher/**, /voucher-order/seckill/**, /upload/**
  → hmdp-voucher-service

GET/POST /voucher-order/get/**, /voucher-order/cancel
  → hmdp-order-service

POST /job/**
  → hmdp-job-service
```

所有非白名单请求由 `AuthGlobalFilter` 校验 Redis Token，并将 `userId` 注入 `X-User-Id` 请求头后转发下游。

---

## 快速启动

### 前置条件

**Docker 部署（推荐）：**

- Docker Engine 24+
- Docker Compose v2

**本地开发：**

- JDK 17+
- Maven 3.9+
- MySQL 8.0（手动执行 `sql/` 下初始化脚本）
- Redis 7+
- Kafka 3.7+（KRaft 模式）
- Eureka Server（本地启动 `hmdp-registry` 模块）

---

### Docker Compose 一键部署

```bash
# 启动全部服务（含基础设施 + 注册中心 + 监控）
docker compose -f infra/docker-compose.yml up -d --build

# 查看所有容器状态
docker compose -f infra/docker-compose.yml ps

# 跟踪指定服务日志
docker compose -f infra/docker-compose.yml logs -f hmdp-gateway

# 停止服务（保留数据卷）
docker compose -f infra/docker-compose.yml down

# 停止服务并清空数据卷
docker compose -f infra/docker-compose.yml down -v
```

**启动顺序：**

```text
MySQL / Redis / Kafka（健康检查就绪）
  → hmdp-registry（Eureka，健康检查就绪）
  → 业务服务（注册至 Eureka）
  → Prometheus / Grafana
```

**访问地址：**

| 地址 | 说明 |
| --- | --- |
| <http://localhost:8080> | API 网关入口 |
| <http://localhost:8761> | Eureka 注册中心控制台 |
| <http://localhost:9090> | Prometheus 指标查询 |
| <http://localhost:3000> | Grafana 监控面板（admin / admin） |

---

### 本地编译

```bash
# 全量编译（跳过测试）
mvn -q -DskipTests compile

# 单独打包某个服务
mvn -pl :hmdp-voucher-service -am package -DskipTests
```

---

## 核心组件说明

### 服务注册与发现（Eureka）

- `hmdp-registry`（Eureka Server）作为独立服务运行，所有业务服务启动时自动注册。
- Feign 客户端通过服务名（如 `hmdp-voucher-service`）调用，Spring Cloud LoadBalancer 负责客户端负载均衡。
- 本地开发无 Eureka 时，可在各服务 `application.yml` 中通过 `hmdp.xxx-service.url` 指定静态地址。

### 熔断降级（Resilience4j）

使用 Feign 的服务（`blog`、`order`、`job`）均已开启 `spring.cloud.openfeign.circuitbreaker.enabled: true`。  
当下游服务不可用时，断路器自动打开，避免请求堆积引发雪崩。

### 统一鉴权（Gateway）

`AuthGlobalFilter` 拦截所有请求：

1. 白名单路径直接放行。
2. 读取 `Authorization` 头中的 Token，查询 Redis Session。
3. 若 Token 有效，刷新过期时间并向下游注入 `X-User-Id` 头；否则返回 `401`。

### 异步下单（Kafka）

秒杀成功后，`hmdp-voucher-service` 投递消息到 Kafka，`hmdp-order-service` 消费后异步创建订单，与秒杀入口完全解耦，保障高并发下的响应速度。

### 监控（Prometheus + Grafana）

各服务已集成 Micrometer，暴露 `/actuator/prometheus` 端点。  
`infra/prometheus.yml` 配置采集目标，Grafana 可导入 **JVM 或 Spring Boot** 官方 Dashboard（ID：4701 / 12900）进行可视化。

---

## Dockerfile 说明

所有可运行模块均采用两阶段构建，构建产物为最小 JRE 镜像：

```text
maven:3.9-eclipse-temurin-17  → 构建指定 Maven 模块
eclipse-temurin:17-jre-jammy  → 运行 Spring Boot fat jar
```

运行时可通过环境变量注入 JVM 参数：

```bash
JAVA_OPTS="-Xms256m -Xmx512m"
```

---

## 前端

前端代码位于 `frontend/hmdp-vue3`，使用 Vue3，开发启动方式以该目录下的 README 为准。
