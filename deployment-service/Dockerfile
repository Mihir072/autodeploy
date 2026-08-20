# ─── Stage 1: Build ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml ./
COPY common/pom.xml ./common/
COPY common/src ./common/src
COPY deployment-service/pom.xml ./deployment-service/
COPY deployment-service/src ./deployment-service/src

RUN ./deployment-service/../mvnw -pl deployment-service -am \
    -Dmaven.test.skip=true \
    --no-transfer-progress \
    package 2>/dev/null || \
    (apk add --no-cache maven && \
     mvn -pl deployment-service -am -Dmaven.test.skip=true --no-transfer-progress package)

# ─── Stage 2: Runtime ───────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# openssh-client needed for SSHJ host key verification
RUN apk add --no-cache openssh-client

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /build/deployment-service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
