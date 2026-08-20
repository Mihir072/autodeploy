-- ─────────────────────────────────────────────────────────────────────────────
-- domain-service V1 — Initial schema
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE domains (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id          UUID NOT NULL,
    user_id             UUID NOT NULL,
    domain_name         VARCHAR(500) UNIQUE NOT NULL,
    -- SUBDOMAIN (auto-assigned) | CUSTOM (user-provided)
    type                VARCHAR(20) NOT NULL CHECK (type IN ('SUBDOMAIN', 'CUSTOM')),
    -- Random token user must add as TXT record _autodeploy-verify.{domain}
    verification_token  VARCHAR(255),
    verified            BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at         TIMESTAMP WITH TIME ZONE,
    -- PENDING | PROVISIONED | FAILED
    ssl_status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- AWS Route53 record set identifier (for cleanup)
    route53_record_id   VARCHAR(255),
    -- ACTIVE | DELETING | DELETED
    status              VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_domains_project_id ON domains(project_id);
CREATE INDEX idx_domains_user_id ON domains(user_id);
CREATE INDEX idx_domains_domain_name ON domains(domain_name);
CREATE INDEX idx_domains_verified ON domains(verified);
CREATE INDEX idx_domains_type ON domains(type);

-- Auto-update trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER domains_updated_at
    BEFORE UPDATE ON domains
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
