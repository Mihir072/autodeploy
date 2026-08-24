# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY mvnw mvnw.cmd ./
COPY .mvn/ .mvn/
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY discovery-service/pom.xml discovery-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY project-service/pom.xml project-service/pom.xml
COPY build-service/pom.xml build-service/pom.xml
COPY deployment-service/pom.xml deployment-service/pom.xml
COPY domain-service/pom.xml domain-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -q

COPY common/ common/
COPY project-service/ project-service/

RUN ./mvnw clean package -pl common,project-service -am -DskipTests -B -q

# ─── Stage 2: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="AutoDeploy <admin@autodeploy.space>"
LABEL service="project-service"

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /workspace/project-service/target/*.jar app.jar

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
