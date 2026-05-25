# Flash Voucher Platform

作者：lrb

Flash Voucher Platform 是一个面向本地生活场景的优惠券秒杀平台，后端基于 Spring Boot 3 / Spring Cloud Gateway / MyBatis-Plus，前端基于 Vue3。项目已经从原始单体结构改造成微服务分层结构，当前重点覆盖登录鉴权、用户、店铺、探店内容、优惠券秒杀、异步下单、订单回滚、通知告警和定时对账。

## 业务架构

```text
用户 / Vue3 前端
        |
        v
hmdp-gateway
        |
        +--> hmdp-auth-service
        |       验证码、登录、登出、当前用户
        |
        +--> hmdp-user-service
        |       用户资料、用户信息、签到
        |
        +--> hmdp-shop-service
        |       店铺、店铺类型、地理位置、店铺缓存
        |
        +--> hmdp-blog-service
        |       探店笔记、评论、点赞、关注、Feed 流
        |
        +--> hmdp-voucher-service
        |       优惠券、秒杀券、秒杀入口、库存预热、缓存失效消息
        |
        +--> hmdp-order-service
        |       券订单、异步下单、订单取消、库存回滚、对账数据
        |
        +--> hmdp-notify-service
        |       回滚告警、自动发券通知、通知接口
        |
        +--> hmdp-job-service
                定时任务、对账调度、手动任务触发
```

核心秒杀链路：

```text
用户抢券
  -> Gateway 鉴权并透传用户上下文
  -> Voucher Service 校验秒杀资格、扣减 Redis 库存、投递 Kafka 消息
  -> Order Service 消费消息并创建券订单
  -> 失败时执行库存回滚和失败日志记录
  -> Job Service 周期性触发对账
  -> Notify Service 处理告警和通知
```

## 代码架构

```text
flash-voucher-platform
├── apps
│   └── hmdp-gateway
│       ├── filter
│       ├── config
│       ├── constant
│       └── Dockerfile
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
│   ├── hmdp-api
│   │   ├── hmdp-api-auth
│   │   ├── hmdp-api-user
│   │   ├── hmdp-api-shop
│   │   ├── hmdp-api-blog
│   │   ├── hmdp-api-voucher
│   │   ├── hmdp-api-order
│   │   └── hmdp-api-notify
│   ├── hmdp-common
│   └── hmdp-parameter
│
├── frameworks
│   ├── hmdp-id-generator-framework
│   ├── hmdp-mq-framework
│   ├── hmdp-redis-tool-framework
│   └── hmdp-redisson-framework
│
├── frontend
│   └── hmdp-vue3
│
├── infra
│   └── docker-compose.yml
│
├── sql
├── docs
├── pom.xml
└── .dockerignore
```

## 服务端口

| 服务 | 端口 | 职责 |
| --- | --- | --- |
| hmdp-gateway | 8080 | 统一入口、鉴权过滤、路由转发 |
| hmdp-auth-service | 8081 | 登录、登出、验证码、当前用户 |
| hmdp-user-service | 8082 | 用户资料、签到 |
| hmdp-shop-service | 8083 | 店铺、店铺类型 |
| hmdp-blog-service | 8084 | 探店笔记、评论、关注、Feed |
| hmdp-voucher-service | 8085 | 优惠券、秒杀入口、库存预热 |
| hmdp-order-service | 8086 | 券订单、异步下单、回滚、对账数据 |
| hmdp-notify-service | 8087 | 通知、告警 |
| hmdp-job-service | 8088 | 定时任务、对账调度 |

## Gateway 路由

```text
/user/code, /user/login, /user/logout, /user/me
  -> hmdp-auth-service

/user/info/**, /user/level/update, /user/sign, /user/sign/count, /user/{id}
  -> hmdp-user-service

/shop/**, /shop-type/**
  -> hmdp-shop-service

/blog/**, /follow/**, /blog-comments/**
  -> hmdp-blog-service

/voucher/**, /voucher-order/seckill/**, /upload/**, /test/**
  -> hmdp-voucher-service

/voucher-order/get/**, /voucher-order/cancel
  -> hmdp-order-service

/job/**
  -> hmdp-job-service
```

## 启动条件

本地源码启动需要：

- JDK 17
- Maven 3.9+
- MySQL 8.0
- Redis 7+
- Kafka 3.7+
- Node.js 18+，仅前端开发需要

Docker 部署需要：

- Docker
- Docker Compose v2

## 本地编译

```bash
mvn -q -DskipTests compile
```

单独打包某个服务：

```bash
mvn -pl :hmdp-voucher-service -am package -DskipTests
```

## Docker 部署

项目已经为后端服务配置 Dockerfile，并提供 Docker Compose 编排文件。

启动全部后端服务和基础设施：

```bash
docker compose -f infra/docker-compose.yml up -d --build
```

查看服务状态：

```bash
docker compose -f infra/docker-compose.yml ps
```

查看日志：

```bash
docker compose -f infra/docker-compose.yml logs -f hmdp-gateway
```

停止服务：

```bash
docker compose -f infra/docker-compose.yml down
```

清理容器和数据卷：

```bash
docker compose -f infra/docker-compose.yml down -v
```

Docker Compose 会启动以下基础设施：

- MySQL 8.0，端口 `3306`
- Redis 7.2，端口 `6379`
- Kafka 3.7，端口 `9092`

MySQL 初始化脚本会从 `sql/` 目录挂载到容器的 `/docker-entrypoint-initdb.d`。

## Dockerfile 说明

每个后端可运行模块都有独立 Dockerfile：

```text
apps/hmdp-gateway/Dockerfile
services/hmdp-auth-service/Dockerfile
services/hmdp-user-service/Dockerfile
services/hmdp-shop-service/Dockerfile
services/hmdp-blog-service/Dockerfile
services/hmdp-voucher-service/Dockerfile
services/hmdp-order-service/Dockerfile
services/hmdp-notify-service/Dockerfile
services/hmdp-job-service/Dockerfile
```

Dockerfile 使用两阶段构建：

```text
maven:3.9-eclipse-temurin-17
  -> 构建指定 Maven 模块及其依赖

eclipse-temurin:17-jre-jammy
  -> 运行打包后的 Spring Boot jar
```

运行时支持通过 `JAVA_OPTS` 注入 JVM 参数，例如：

```bash
JAVA_OPTS="-Xms256m -Xmx512m"
```

## 前端

前端代码位于：

```text
frontend/hmdp-vue3
```

前端开发启动方式以该目录下的 README 为准。

## 说明

当前项目定位是可本地运行、可 Docker 编排的微服务版本。生产环境还需要继续补充镜像仓库、配置中心、服务发现、日志采集、链路追踪、监控告警和密钥管理等能力。
