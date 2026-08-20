# ─── Stage 1: Build ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml ./
COPY common/pom.xml ./common/
COPY discovery-service/pom.xml ./discovery-service/
COPY api-gateway/pom.xml ./api-gateway/
COPY auth-service/pom.xml ./auth-service/
COPY project-service/pom.xml ./project-service/
COPY build-service/pom.xml ./build-service/
COPY deployment-service/pom.xml ./deployment-service/
COPY domain-service/pom.xml ./domain-service/
COPY notification-service/pom.xml ./notification-service/

COPY common/src ./common/src
COPY discovery-service/src ./discovery-service/src

# Build only this service and its dependencies (skips other modules)
RUN ./discovery-service/../mvnw -pl discovery-service -am \
    -Dmaven.test.skip=true \
    --no-transfer-progress \
    package 2>/dev/null || \
    (apk add --no-cache maven && \
     mvn -pl discovery-service -am -Dmaven.test.skip=true --no-transfer-progress package)

# ─── Stage 2: Runtime ───────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the fat JAR from build stage
COPY --from=builder /build/discovery-service/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
