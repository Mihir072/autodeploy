# ─────────────────────────────────────────────────────────────────────────────
# Terraform Outputs — Cost-Optimized EC2 Infrastructure
# ─────────────────────────────────────────────────────────────────────────────

output "server_public_ip" {
  description = "Elastic Public IP of the EC2 Server"
  value       = aws_eip.server_eip.public_ip
}

output "server_instance_id" {
  description = "EC2 Instance ID"
  value       = aws_instance.server.id
}

output "ecr_registry_url" {
  description = "ECR registry URL (account.dkr.ecr.region.amazonaws.com)"
  value       = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
}

output "route53_nameservers" {
  description = "Route53 nameservers — update your domain registrar with these"
  value       = aws_route53_zone.main.name_servers
}

output "ci_cd_access_key_id" {
  description = "CI/CD IAM user access key ID (for GitHub Secrets)"
  value       = aws_iam_access_key.ci_cd.id
}

output "ci_cd_secret_access_key" {
  description = "CI/CD IAM user secret access key (for GitHub Secrets)"
  value       = aws_iam_access_key.ci_cd.secret
  sensitive   = true
}

output "ssh_command" {
  description = "SSH command to connect to the server"
  value       = "ssh ubuntu@${aws_eip.server_eip.public_ip}"
}
