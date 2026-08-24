# ─────────────────────────────────────────────────────────────────────────────
# Terraform Variables — Cost-Optimized EC2 Infrastructure
# ─────────────────────────────────────────────────────────────────────────────

variable "aws_region" {
  description = "AWS region for all resources"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "autodeploy"
}

variable "environment" {
  description = "Environment name (e.g., prod, staging)"
  type        = string
  default     = "prod"
}

variable "domain_name" {
  description = "Primary domain name"
  type        = string
  default     = "autodeploy.space"
}

# ── EC2 Configuration ────────────────────────────────────────────────────────
variable "ami_id" {
  description = "Ubuntu 24.04 LTS AMI ID for us-east-1"
  type        = string
  default     = "ami-04a81a99f5ec58529"
}

variable "ec2_instance_type" {
  description = "EC2 instance type (Free Tier eligible: t3.micro or t2.micro)"
  type        = string
  default     = "t3.micro"
}

variable "ec2_root_volume_size" {
  description = "Root disk size in GB"
  type        = number
  default     = 60
}

variable "ssh_public_key" {
  description = "Public SSH key for EC2 instance access"
  type        = string
  default     = ""
}

# ── VPC Configuration ────────────────────────────────────────────────────────
variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}
