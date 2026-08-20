# ─── Stage 1: Build ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml ./
COPY common/pom.xml ./common/
COPY common/src ./common/src
COPY notification-service/pom.xml ./notification-service/
COPY notification-service/src ./notification-service/src

RUN ./notification-service/../mvnw -pl notification-service -am \
    -Dmaven.test.skip=true \
    --no-transfer-progress \
    package 2>/dev/null || \
    (apk add --no-cache maven && \
     mvn -pl notification-service -am -Dmaven.test.skip=true --no-transfer-progress package)

# ─── Stage 2: Runtime ───────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /build/notification-service/target/*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
