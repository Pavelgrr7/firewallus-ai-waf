CREATE TABLE waf_settings (
                              id INT PRIMARY KEY CHECK (id = 1),
                              rate_limit_requests INT NOT NULL DEFAULT 2000,
                              rate_limit_window_sec INT NOT NULL DEFAULT 60,

                              adaptive_mode_enabled BOOLEAN NOT NULL DEFAULT false, -- на будущее

                              updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                              updated_by UUID REFERENCES admins(admin_id) ON DELETE SET NULL,
                              target_url VARCHAR(255) NOT NULL
);
