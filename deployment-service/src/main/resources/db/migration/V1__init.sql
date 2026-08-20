-- ─────────────────────────────────────────────────────────────────────────────
-- deployment-service V1 — Initial schema
-- ─────────────────────────────────────────────────────────────────────────────

-- Pool of EC2 instances managed by this service
CREATE TABLE ec2_instances (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- AWS instance ID (e.g. i-0abc123def456789)
    instance_id         VARCHAR(50) UNIQUE NOT NULL,
    host                VARCHAR(255) NOT NULL,  -- public IP or DNS
    ssh_port            INT NOT NULL DEFAULT 22,
    -- ACTIVE | DRAINING | OFFLINE
    status              VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    active_deployments  INT NOT NULL DEFAULT 0,
    max_deployments     INT NOT NULL DEFAULT 10,
    region              VARCHAR(50) NOT NULL DEFAULT 'us-east-1',
    tags                JSONB,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE deployments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID NOT NULL,
    build_id        UUID NOT NULL,
    user_id         UUID NOT NULL,
    -- The EC2 instance this was deployed to
    ec2_instance_id UUID REFERENCES ec2_instances(id),
    -- Docker container ID on the EC2 instance
    container_id    VARCHAR(100),
    -- Full ECR image URI that was deployed
    image_uri       VARCHAR(500),
    image_tag       VARCHAR(255),
    -- PENDING | RUNNING | SUCCESS | FAILED | ROLLED_BACK
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- Generated subdomain (e.g. "my-app-3a8f1c2b")
    subdomain       VARCHAR(255),
    error_message   TEXT,
    -- Previous image tag (for rollback support)
    previous_image_tag VARCHAR(255),
    deployed_at     TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_deployments_project_id ON deployments(project_id);
CREATE INDEX idx_deployments_build_id ON deployments(build_id);
CREATE INDEX idx_deployments_user_id ON deployments(user_id);
CREATE INDEX idx_deployments_status ON deployments(status);
CREATE INDEX idx_deployments_created_at ON deployments(created_at DESC);
CREATE INDEX idx_ec2_instances_status ON ec2_instances(status);

-- Auto-update trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER deployments_updated_at
    BEFORE UPDATE ON deployments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER ec2_instances_updated_at
    BEFORE UPDATE ON ec2_instances
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed a default local EC2 instance for development (override in prod)
INSERT INTO ec2_instances (instance_id, host, status, max_deployments, region)
VALUES ('i-local-dev', 'localhost', 'ACTIVE', 50, 'us-east-1')
ON CONFLICT (instance_id) DO NOTHING;
