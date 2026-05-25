# ── Stage 1: Build ────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

# 拷贝全量源码，Maven 多模块需要从根目录编译以解析内部依赖
COPY . .

# 只构建 hmdp-gateway 及其所有内部依赖模块（-am = also-make）
RUN mvn -pl :hmdp-gateway -am package -DskipTests -B --no-transfer-progress

# ── Stage 2: Runtime ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /workspace/apps/hmdp-gateway/target/hmdp-gateway-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
