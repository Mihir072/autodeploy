# AutoDeploy Platform

> A CI/CD auto-deployment platform built with Java 21 + Spring Boot 3 microservices — similar to Vercel/Render.

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                        Frontend (React)                           │
└────────────────────────────┬─────────────────────────────────────┘
                             │ HTTP + WebSocket
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│                    api-gateway :9090                              │
│         JWT Auth Filter · Rate Limiting · Routing                 │
└──┬────────┬──────────┬──────────┬─────────┬──────────────────────┘
   │        │          │          │         │
   ▼        ▼          ▼          ▼         ▼
auth     project    build    deployment  domain   notification
:8081    :8082      :8083     :8084      :8085     :8086
   │        │          │          │         │          │
   └────────┴──────────┴──────────┴─────────┴──────────┘
                             │
                    ┌────────┴────────┐
                    │   Eureka :8761  │  Service Discovery
                    └─────────────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
          Postgres         Redis        RabbitMQ
        (6 databases)   (pub/sub)     (async jobs)
```

## Services & Ports

| Service | Port | Description |
|---------|------|-------------|
| `discovery-service` | 8761 | Eureka service registry |
| `api-gateway` | 9090 | JWT auth + routing (external) |
| `auth-service` | 8081 | GitHub OAuth2 + JWT issuance |
| `project-service` | 8082 | Repo management + webhooks |
| `build-service` | 8083 | Kaniko builds + ECR push |
| `deployment-service` | 8084 | EC2 SSH deployment |
| `domain-service` | 8085 | Route53 DNS + Let's Encrypt |
| `notification-service` | 8086 | WebSocket/SSE live streaming |
| RabbitMQ UI | 15672 | guest/guest (local dev) |
| Traefik Dashboard | 8080 | Local reverse proxy |

## Quick Start (Local Dev)

### 1. Prerequisites
- Java 21 ([Eclipse Temurin](https://adoptium.net/))
- Maven 3.9+
- Docker + Docker Compose

### 2. Set up environment
```bash
cp .env.example .env
# Edit .env with your GitHub OAuth credentials and generated secrets

# Generate JWT secret (512-bit minimum for HS512)
openssl rand -base64 64

# Generate AES-256 encryption key
openssl rand -base64 32
```

### 3. Start infrastructure only
```bash
docker-compose -f docker-compose.infra.yml up -d
```

### 4. Run services from IDE
Import the root `pom.xml` into IntelliJ IDEA as a Maven project.
Each service has its own `Application` main class. Run them in this order:
1. `DiscoveryServiceApplication` (Eureka must start first)
2. Any order for the rest

### 5. Or run the full stack with Docker
```bash
# Build all service JARs
mvn clean package -DskipTests

# Start everything
docker-compose up -d
```

## Module Structure

```
auto-deploy/
├── pom.xml                    ← Root aggregator POM
├── docker-compose.yml         ← Full stack (all services + infra)
├── docker-compose.infra.yml   ← Infra only (for IDE dev)
├── postgres-init/
│   └── init.sql               ← Creates all 6 service databases
├── .env.example               ← Environment variable template
│
├── common/                    ← Shared library (JWT, AES, DTOs, events)
│   └── src/main/java/com/autodeploy/common/
│       ├── config/            ← JwtProperties, EncryptionProperties, AutoConfig
│       ├── constants/         ← RabbitMQConstants, RedisKeyConstants
│       ├── dto/               ← ApiResponse<T>, ErrorResponse, PageResponse<T>
│       ├── event/             ← Build/Deployment RabbitMQ events
│       ├── exception/         ← BaseException hierarchy
│       ├── security/          ← JwtTokenProvider, JwtClaims
│       └── util/              ← AesEncryptionUtil, SlugUtil
│
├── discovery-service/         ← Eureka server
├── api-gateway/               ← Spring Cloud Gateway + JWT filter
├── auth-service/              ← GitHub OAuth2 + JWT issuance
├── project-service/           ← Project CRUD + GitHub webhooks
├── build-service/             ← Kaniko builds + ECR push
├── deployment-service/        ← EC2 SSH deployment + rollback
├── domain-service/            ← Route53 DNS + custom domains
└── notification-service/      ← WebSocket/SSE + email notifications
```

## Message Flow

```
GitHub Push → project-service (webhook)
                    │
                    ▼ RabbitMQ: build.requested
             build-service
                    │ Redis Pub/Sub: build-logs:{buildId}
                    │                    ↓
                    │           notification-service → WebSocket → Browser
                    │
                    ▼ RabbitMQ: build.completed
          deployment-service
                    │ SSH → EC2: docker run (Traefik labels)
                    │
                    ▼ RabbitMQ: deployment.completed
          notification-service → Email
                    │
          domain-service → Route53 A record (auto-subdomain)
```

## Security

- GitHub tokens and project env vars are encrypted at rest with **AES-256-GCM**
- Builds run via **Kaniko** (rootless, no Docker daemon access)
- JWT validated at the **gateway level** only; downstream services trust `X-User-Id` header
- Rate limiting: **20 req/s** per user, burst to 40 (Redis-backed)

## Build for Production

```bash
# Build all JARs
mvn clean package -DskipTests

# Build Docker images (from root — uses multi-stage Dockerfiles)
docker build -f auth-service/Dockerfile -t autodeploy/auth-service .
docker build -f api-gateway/Dockerfile -t autodeploy/api-gateway .
# ... repeat for each service
```

## Next Steps

Follow the service build sequence:
2. `auth-service` — GitHub OAuth2 flow and JWT issuance
3. `project-service` — GitHub repo listing and project CRUD
4. `build-service` — Kaniko integration and RabbitMQ consumer
5. `deployment-service` — EC2 SSH deployment and Traefik label generation
6. `domain-service` — Route53 and Let's Encrypt integration
7. `notification-service` — Redis Pub/Sub and WebSocket streaming
8. `api-gateway` — Routing and JWT validation filter
9. Terraform scripts for AWS infrastructure
