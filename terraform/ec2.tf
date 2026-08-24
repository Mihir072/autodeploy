# ─────────────────────────────────────────────────────────────────────────────
# EC2 — Single Instance setup with Docker & Docker Compose pre-installed
# ─────────────────────────────────────────────────────────────────────────────

# ── Key Pair (Optional SSH Key) ─────────────────────────────────────────────
resource "aws_key_pair" "deployer" {
  count      = var.ssh_public_key != "" ? 1 : 0
  key_name   = "${var.project_name}-key"
  public_key = var.ssh_public_key
}

# ── Security Group ───────────────────────────────────────────────────────────
resource "aws_security_group" "ec2_sg" {
  name        = "${var.project_name}-ec2-sg"
  description = "Security group for AutoDeploy EC2 server"
  vpc_id      = aws_vpc.main.id

  # HTTP
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP"
  }

  # HTTPS
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS"
  }

  # SSH
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "SSH"
  }

  # Outbound to all
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-ec2-sg"
  }
}

# ── EC2 Instance ─────────────────────────────────────────────────────────────
resource "aws_instance" "server" {
  ami           = data.aws_ami.ubuntu.id
  instance_type = var.ec2_instance_type

  subnet_id                   = aws_subnet.public[0].id
  vpc_security_group_ids      = [aws_security_group.ec2_sg.id]
  associate_public_ip_address = true

  key_name = var.ssh_public_key != "" ? aws_key_pair.deployer[0].key_name : null

  root_block_device {
    volume_size           = var.ec2_root_volume_size
    volume_type           = "gp3"
    encrypted             = true
    delete_on_termination = false
  }

  # Cloud-init script: install Docker & Docker Compose
  user_data = <<-EOF
              #!/bin/bash
              set -e

              # Update & install dependencies
              apt-get update -y
              apt-get install -y ca-certificates curl gnupg lsb-release git jq

              # Add Docker's official GPG key
              install -m 0755 -d /etc/apt/keyrings
              curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
              chmod a+r /etc/apt/keyrings/docker.asc

              # Add Docker repository
              echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

              # Install Docker Engine & Docker Compose V2
              apt-get update -y
              apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

              # Enable & start Docker
              systemctl enable docker
              systemctl start docker
              usermod -aG docker ubuntu

              # Create application directory
              mkdir -p /opt/autodeploy
              chown -R ubuntu:ubuntu /opt/autodeploy

              echo "AutoDeploy EC2 Setup Complete!" > /tmp/ec2-setup-complete.txt
              EOF

  tags = {
    Name = "${var.project_name}-server"
  }
}

# ── Elastic IP (Static Public IP) ────────────────────────────────────────────
resource "aws_eip" "server_eip" {
  instance = aws_instance.server.id
  domain   = "vpc"

  tags = {
    Name = "${var.project_name}-eip"
  }
}
