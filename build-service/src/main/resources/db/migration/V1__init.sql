-- ─────────────────────────────────────────────────────────────────────────────
-- build-service V1 — Initial schema
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE builds (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Cross-service references (no FK constraints — query project-service via REST)
    project_id      UUID NOT NULL,
    user_id         UUID NOT NULL,
    -- The git commit SHA that triggered this build (full 40-char)
    commit_sha      VARCHAR(40) NOT NULL,
    commit_message  TEXT,
    branch          VARCHAR(255),
    -- QUEUED | RUNNING | SUCCESS | FAILED | CANCELLED
    status          VARCHAR(50) NOT NULL DEFAULT 'QUEUED',
    -- Full ECR image URI pushed: 123456.dkr.ecr.us-east-1.amazonaws.com/autodeploy:{tag}
    image_uri       VARCHAR(500),
    -- Short tag: {projectId[0..7]}-{commitSha[0..7]}
    image_tag       VARCHAR(255),
    error_message   TEXT,
    -- MANUAL | WEBHOOK | API
    triggered_by    VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    started_at      TIMESTAMP WITH TIME ZONE,
    finished_at     TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_builds_project_id ON builds(project_id);
CREATE INDEX idx_builds_user_id ON builds(user_id);
CREATE INDEX idx_builds_status ON builds(status);
CREATE INDEX idx_builds_commit_sha ON builds(commit_sha);
CREATE INDEX idx_builds_created_at ON builds(created_at DESC);
