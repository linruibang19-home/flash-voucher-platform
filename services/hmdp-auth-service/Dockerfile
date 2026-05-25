FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY . .

RUN mvn -pl :hmdp-auth-service -am package -DskipTests -B --no-transfer-progress

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /workspace/services/hmdp-auth-service/target/hmdp-auth-service-*.jar app.jar

EXPOSE 8081
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
