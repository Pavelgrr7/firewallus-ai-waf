CREATE TYPE IP_LIST_TYPE AS ENUM ('BLACKLIST', 'WHITELIST');

CREATE TABLE ip_lists (
                          id UUID PRIMARY KEY,
                          ip_address VARCHAR(45) NOT NULL UNIQUE,
                          list_type IP_LIST_TYPE NOT NULL,
                          description VARCHAR(255),
                          created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                          created_by UUID REFERENCES admins(admin_id) ON DELETE SET NULL
);

CREATE INDEX idx_ip_lists_address ON ip_lists(ip_address);