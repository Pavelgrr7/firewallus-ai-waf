ALTER TABLE waf_settings
    ADD COLUMN tg_bot_token VARCHAR(255),
    ADD COLUMN tg_chat_id VARCHAR(255),
    ADD COLUMN alert_threshold INT NOT NULL DEFAULT 50;