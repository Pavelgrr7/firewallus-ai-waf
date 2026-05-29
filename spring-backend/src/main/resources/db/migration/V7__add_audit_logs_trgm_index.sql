CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_audit_logs_rule_name_trgm ON audit_logs USING gin (rule_name gin_trgm_ops);
