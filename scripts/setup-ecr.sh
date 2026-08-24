#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# setup-ecr.sh — Automatically create all required AWS ECR repositories
# ─────────────────────────────────────────────────────────────────────────────

set -euo pipefail

AWS_REGION="${AWS_REGION:-us-east-1}"

REPOS=(
    "autodeploy-discovery"
    "autodeploy-gateway"
    "autodeploy-auth"
    "autodeploy-project"
    "autodeploy-build"
    "autodeploy-deployment"
    "autodeploy-domain"
    "autodeploy-notification"
    "autodeploy-frontend"
)

echo "Ensuring all AWS ECR repositories exist in region $AWS_REGION..."

for repo in "${REPOS[@]}"; do
    if aws ecr describe-repositories --repository-names "$repo" --region "$AWS_REGION" >/dev/null 2>&1; then
        echo " [EXISTS]  $repo"
    else
        echo " [CREATING] $repo..."
        aws ecr create-repository --repository-name "$repo" --region "$AWS_REGION" --image-scanning-configuration scanOnPush=true >/dev/null
        echo " [CREATED]  $repo"
    fi
done

echo "All ECR repositories are ready!"
