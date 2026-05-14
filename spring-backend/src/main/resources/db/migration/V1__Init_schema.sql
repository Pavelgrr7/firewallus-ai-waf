CREATE TYPE ACTIONTYPE AS ENUM ('BLOCK', 'ALLOW', 'LOG');
CREATE TYPE AUDIT_ACTION AS ENUM ('CREATE_RULE', 'UPDATE_RULE', 'DELETE_RULE', 'ENABLE_RULE', 'DISABLE_RULE', 'LOGIN');

CREATE TABLE admins
(
    admin_id      UUID PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE rules
(
    rule_id    SERIAL PRIMARY KEY,
    name       VARCHAR(64) UNIQUE NOT NULL,
    action     ACTIONTYPE         NOT NULL DEFAULT 'BLOCK',
    conditions JSONB              NOT NULL DEFAULT '[]'::jsonb,
    is_active  BOOLEAN            NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ                 DEFAULT CURRENT_TIMESTAMP,
    created_by UUID               REFERENCES admins (admin_id) ON DELETE SET NULL
);

CREATE TABLE audit_logs
(
    action_id UUID PRIMARY KEY,
    admin_id  UUID         REFERENCES admins (admin_id) ON DELETE SET NULL,
    action    AUDIT_ACTION NOT NULL,
    timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    rule_id   INT          REFERENCES rules (rule_id) ON DELETE SET NULL,
    rule_name VARCHAR(64)  NOT NULL
);

CREATE TABLE incident_logs
(
    incident_id      UUID PRIMARY KEY,
    incident_type    VARCHAR(64) NOT NULL,
    timestamp        TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    attacker_ip      VARCHAR(45) NOT NULL,
    confidence_score REAL,
    payload_dump     JSONB
);

CREATE INDEX idx_rules_created_by ON rules(created_by);
CREATE INDEX idx_audit_logs_admin_id ON audit_logs(admin_id);
CREATE INDEX idx_audit_logs_rule_id ON audit_logs(rule_id);
CREATE INDEX idx_incident_logs_ip ON incident_logs(attacker_ip);
CREATE INDEX idx_incident_logs_time ON incident_logs(timestamp DESC);
CREATE INDEX idx_audit_logs_time ON audit_logs(timestamp DESC);