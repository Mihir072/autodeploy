#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# deploy.sh — AutoDeploy AWS EC2 Deployment Helper
#
# Usage:
#   ./scripts/deploy.sh setup-infra    # Provision EC2 & DNS with Terraform
#   ./scripts/deploy.sh sync           # Sync production docker-compose.prod.yml & .env to EC2
#   ./scripts/deploy.sh build-push     # Build and push Docker images to ECR
#   ./scripts/deploy.sh deploy-remote  # SSH to EC2 and start containers
#   ./scripts/deploy.sh status         # Check remote status via SSH
#   ./scripts/deploy.sh logs <svc>     # Tail logs on EC2
# ─────────────────────────────────────────────────────────────────────────────

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_ok()    { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
AWS_REGION="${AWS_REGION:-us-east-1}"

# ── Prerequisite checks ──────────────────────────────────────────────────────
check_prerequisites() {
    local missing=0
    for cmd in aws docker terraform; do
        if ! command -v "$cmd" &> /dev/null; then
            log_error "Missing required tool: $cmd"
            missing=1
        fi
    done
    if [[ $missing -eq 1 ]]; then
        log_error "Install missing tools and retry."
        exit 1
    fi
    log_ok "All prerequisites found."
}

# ── Setup Infrastructure ─────────────────────────────────────────────────────
setup_infra() {
    log_info "Provisioning EC2 server and Route53 DNS with Terraform..."
    cd "$PROJECT_ROOT/terraform"

    if [[ ! -f terraform.tfvars ]]; then
        log_error "terraform.tfvars not found. Copy terraform.tfvars.example to terraform.tfvars."
        exit 1
    fi

    terraform init
    terraform apply -auto-approve

    log_ok "EC2 Server & DNS provisioned!"
    log_info "Server Public IP: $(terraform output -raw server_public_ip)"
    log_info "SSH Command: $(terraform output -raw ssh_command)"
}

# ── Sync Config to EC2 ───────────────────────────────────────────────────────
sync_config() {
    local SERVER_IP
    SERVER_IP=$(cd "$PROJECT_ROOT/terraform" && terraform output -raw server_public_ip)

    log_info "Syncing production files to EC2 server ($SERVER_IP)..."
    scp "$PROJECT_ROOT/docker-compose.prod.yml" "ubuntu@$SERVER_IP:/opt/autodeploy/docker-compose.prod.yml"
    scp "$PROJECT_ROOT/.env" "ubuntu@$SERVER_IP:/opt/autodeploy/.env"
    scp -r "$PROJECT_ROOT/postgres-init" "ubuntu@$SERVER_IP:/opt/autodeploy/"

    log_ok "Configuration files synced to /opt/autodeploy on server."
}

# ── Build & Push ─────────────────────────────────────────────────────────────
build_push() {
    log_info "Building and pushing Docker images to ECR..."

    ECR_REGISTRY=$(cd "$PROJECT_ROOT/terraform" && terraform output -raw ecr_registry_url)
    IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD)}"

    # Login to ECR
    aws ecr get-login-password --region "$AWS_REGION" | \
        docker login --username AWS --password-stdin "$ECR_REGISTRY"

    # Backend services
    declare -A SERVICES=(
        ["autodeploy-discovery"]="discovery-service/Dockerfile"
        ["autodeploy-gateway"]="api-gateway/Dockerfile"
        ["autodeploy-auth"]="auth-service/Dockerfile"
        ["autodeploy-project"]="project-service/Dockerfile"
        ["autodeploy-build"]="build-service/Dockerfile"
        ["autodeploy-deployment"]="deployment-service/Dockerfile"
        ["autodeploy-domain"]="domain-service/Dockerfile"
        ["autodeploy-notification"]="notification-service/Dockerfile"
    )

    cd "$PROJECT_ROOT"
    for name in "${!SERVICES[@]}"; do
        log_info "Building $name..."
        docker build -t "$ECR_REGISTRY/$name:$IMAGE_TAG" \
                     -t "$ECR_REGISTRY/$name:latest" \
                     -f "${SERVICES[$name]}" .
        docker push "$ECR_REGISTRY/$name:$IMAGE_TAG"
        docker push "$ECR_REGISTRY/$name:latest"
        log_ok "$name pushed."
    done

    # Frontend
    log_info "Building frontend..."
    docker build -t "$ECR_REGISTRY/autodeploy-frontend:$IMAGE_TAG" \
                 -t "$ECR_REGISTRY/autodeploy-frontend:latest" \
                 --build-arg VITE_API_BASE_URL=https://api.autodeploy.space \
                 --build-arg VITE_WS_URL=wss://api.autodeploy.space \
                 --build-arg VITE_GITHUB_AUTH_URL=https://api.autodeploy.space/oauth2/authorization/github \
                 -f frontend/Dockerfile frontend/
    docker push "$ECR_REGISTRY/autodeploy-frontend:$IMAGE_TAG"
    docker push "$ECR_REGISTRY/autodeploy-frontend:latest"
    log_ok "Frontend pushed."

    log_ok "All images pushed!"
}

# ── Deploy on EC2 Server ─────────────────────────────────────────────────────
deploy_remote() {
    local SERVER_IP
    SERVER_IP=$(cd "$PROJECT_ROOT/terraform" && terraform output -raw server_public_ip)
    ECR_REGISTRY=$(cd "$PROJECT_ROOT/terraform" && terraform output -raw ecr_registry_url)

    log_info "Triggering remote docker compose up on EC2 ($SERVER_IP)..."

    ssh "ubuntu@$SERVER_IP" << EOF
        cd /opt/autodeploy
        aws ecr get-login-password --region $AWS_REGION | sudo docker login --username AWS --password-stdin $ECR_REGISTRY
        sudo ECR_REGISTRY_URL=$ECR_REGISTRY docker compose -f docker-compose.prod.yml pull
        sudo ECR_REGISTRY_URL=$ECR_REGISTRY docker compose -f docker-compose.prod.yml up -d
        sudo docker image prune -f
EOF

    log_ok "Deployment started on EC2!"
}

# ── Status ───────────────────────────────────────────────────────────────────
status() {
    local SERVER_IP
    SERVER_IP=$(cd "$PROJECT_ROOT/terraform" && terraform output -raw server_public_ip)
    log_info "Checking container status on EC2 ($SERVER_IP)..."
    ssh "ubuntu@$SERVER_IP" "cd /opt/autodeploy && sudo docker compose -f docker-compose.prod.yml ps"
}

# ── Logs ─────────────────────────────────────────────────────────────────────
logs() {
    local service="${1:?Usage: deploy.sh logs <service-name>}"
    local SERVER_IP
    SERVER_IP=$(cd "$PROJECT_ROOT/terraform" && terraform output -raw server_public_ip)
    ssh "ubuntu@$SERVER_IP" "cd /opt/autodeploy && sudo docker compose -f docker-compose.prod.yml logs -f --tail=100 $service"
}

# ── Main ─────────────────────────────────────────────────────────────────────
main() {
    check_prerequisites

    case "${1:-help}" in
        setup-infra)    setup_infra ;;
        sync)           sync_config ;;
        build-push)     build_push ;;
        deploy-remote)  deploy_remote ;;
        status)         status ;;
        logs)           shift; logs "$@" ;;
        *)
            echo "Usage: $0 {setup-infra|sync|build-push|deploy-remote|status|logs <svc>}"
            exit 1
            ;;
    esac
}

main "$@"
