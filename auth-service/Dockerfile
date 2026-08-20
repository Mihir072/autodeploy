# ─── Stage 1: Build ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml ./
COPY common/pom.xml ./common/
COPY common/src ./common/src
COPY auth-service/pom.xml ./auth-service/
COPY auth-service/src ./auth-service/src

RUN ./auth-service/../mvnw -pl auth-service -am \
    -Dmaven.test.skip=true \
    --no-transfer-progress \
    package 2>/dev/null || \
    (apk add --no-cache maven && \
     mvn -pl auth-service -am -Dmaven.test.skip=true --no-transfer-progress package)

# ─── Stage 2: Runtime ───────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /build/auth-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
