-- ─────────────────────────────────────────────────────────────────────────────
-- project-service V1 — Initial schema
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE projects (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- References users.id in auth-service (cross-service, no FK)
    user_id                 UUID NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    -- URL-safe slug for subdomain generation (e.g. "my-app-3a8f1c2b")
    slug                    VARCHAR(255) UNIQUE NOT NULL,
    -- GitHub "owner/repo-name"
    repo_full_name          VARCHAR(500) NOT NULL,
    repo_url                VARCHAR(500) NOT NULL,
    branch                  VARCHAR(255) NOT NULL DEFAULT 'main',
    dockerfile_path         VARCHAR(500),
    build_command           VARCHAR(1000),
    -- AES-256-GCM encrypted JSON map of env vars
    env_vars_encrypted      TEXT,
    -- INACTIVE | BUILDING | DEPLOYING | ACTIVE | FAILED | ARCHIVED
    status                  VARCHAR(50) NOT NULL DEFAULT 'INACTIVE',
    -- GitHub webhook ID for this project (used to delete webhook on project delete)
    github_webhook_id       BIGINT,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_projects_user_id ON projects(user_id);
CREATE INDEX idx_projects_slug ON projects(slug);
CREATE INDEX idx_projects_repo_full_name ON projects(repo_full_name);
CREATE INDEX idx_projects_status ON projects(status);

-- Auto-update trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER projects_updated_at
    BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
