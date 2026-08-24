# ─────────────────────────────────────────────────────────────────────────────
# ECR — Elastic Container Registry repositories for all services
# ─────────────────────────────────────────────────────────────────────────────

locals {
  ecr_repositories = [
    "autodeploy-discovery",
    "autodeploy-gateway",
    "autodeploy-auth",
    "autodeploy-project",
    "autodeploy-build",
    "autodeploy-deployment",
    "autodeploy-domain",
    "autodeploy-notification",
    "autodeploy-frontend",
  ]
}

resource "aws_ecr_repository" "services" {
  for_each = toset(local.ecr_repositories)

  name                 = each.value
  image_tag_mutability = "MUTABLE"
  force_delete         = false

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name = each.value
  }
}

# ── Lifecycle Policy — keep only last 10 images per repo ──────────────────────
resource "aws_ecr_lifecycle_policy" "cleanup" {
  for_each   = toset(local.ecr_repositories)
  repository = aws_ecr_repository.services[each.key].name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep only the last 10 images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 10
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}
