# ─────────────────────────────────────────────────────────────────────────────
# IAM — CI/CD User for ECR Push access
# ─────────────────────────────────────────────────────────────────────────────

resource "aws_iam_user" "ci_cd" {
  name = "${var.project_name}-ci-cd"

  tags = {
    Name = "${var.project_name}-ci-cd-user"
  }
}

resource "aws_iam_user_policy" "ci_cd_ecr" {
  name = "${var.project_name}-ci-cd-ecr-policy"
  user = aws_iam_user.ci_cd.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:DescribeRepositories",
          "ecr:ListImages",
          "ecr:CreateRepository"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_access_key" "ci_cd" {
  user = aws_iam_user.ci_cd.name
}
