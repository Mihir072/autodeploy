# ─────────────────────────────────────────────────────────────────────────────
# Terraform Main — Provider Configuration & Backend
# ─────────────────────────────────────────────────────────────────────────────

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Remote state storage in S3 (optional — create bucket first or comment out for local state)
  # backend "s3" {
  #   bucket         = "autodeploy-terraform-state"
  #   key            = "prod/terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "autodeploy-terraform-locks"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# ── Data Sources ──────────────────────────────────────────────────────────────
data "aws_caller_identity" "current" {}


