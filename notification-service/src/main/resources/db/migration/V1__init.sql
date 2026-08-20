-- ─────────────────────────────────────────────────────────────────────────────
-- notification-service V1 — Initial schema
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE notification_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    -- DEPLOY | BUILD | SYSTEM
    event_type      VARCHAR(50) NOT NULL,
    -- The ID of the resource this notification is about (build ID, deployment ID, etc.)
    resource_id     UUID,
    resource_type   VARCHAR(50),
    title           VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    -- EMAIL | WEBSOCKET | SSE
    channel         VARCHAR(50) NOT NULL,
    -- SUCCESS | FAILURE | INFO
    severity        VARCHAR(20) NOT NULL DEFAULT 'INFO',
    sent_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    read_at         TIMESTAMP WITH TIME ZONE
);

CREATE TABLE notification_preferences (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID UNIQUE NOT NULL,
    email_on_success    BOOLEAN NOT NULL DEFAULT TRUE,
    email_on_failure    BOOLEAN NOT NULL DEFAULT TRUE,
    email_address       VARCHAR(255),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_notification_history_user_id ON notification_history(user_id);
CREATE INDEX idx_notification_history_resource ON notification_history(resource_type, resource_id);
CREATE INDEX idx_notification_history_sent_at ON notification_history(sent_at DESC);
CREATE INDEX idx_notification_preferences_user_id ON notification_preferences(user_id);

-- Auto-update trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER notification_preferences_updated_at
    BEFORE UPDATE ON notification_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
