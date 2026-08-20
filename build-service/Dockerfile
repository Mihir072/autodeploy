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
COPY build-service/src ./build-service/src

RUN ./build-service/../mvnw -pl build-service -am \
    -Dmaven.test.skip=true \
    --no-transfer-progress \
    package 2>/dev/null || \
    (apk add --no-cache maven && \
     mvn -pl build-service -am -Dmaven.test.skip=true --no-transfer-progress package)

# ─── Stage 2: Runtime ───────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# git is needed for shallow clones inside the container
RUN apk add --no-cache git

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /build/build-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
