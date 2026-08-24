# ─────────────────────────────────────────────────────────────────────────────
# Route53 — DNS Records pointing autodeploy.space to EC2 Elastic IP
# ─────────────────────────────────────────────────────────────────────────────

# Hosted Zone for autodeploy.space
resource "aws_route53_zone" "main" {
  name = var.domain_name

  tags = {
    Name = "${var.project_name}-hosted-zone"
  }
}

# A Record: autodeploy.space -> EC2 Elastic IP
resource "aws_route53_record" "apex" {
  zone_id = aws_route53_zone.main.zone_id
  name    = var.domain_name
  type    = "A"
  ttl     = 300
  records = [aws_eip.server_eip.public_ip]
}

# A Record: api.autodeploy.space -> EC2 Elastic IP
resource "aws_route53_record" "api" {
  zone_id = aws_route53_zone.main.zone_id
  name    = "api.${var.domain_name}"
  type    = "A"
  ttl     = 300
  records = [aws_eip.server_eip.public_ip]
}

# CNAME Record: www.autodeploy.space -> autodeploy.space
resource "aws_route53_record" "www" {
  zone_id = aws_route53_zone.main.zone_id
  name    = "www.${var.domain_name}"
  type    = "CNAME"
  ttl     = 300
  records = [var.domain_name]
}
