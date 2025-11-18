CREATE TABLE IF NOT EXISTS roguelike_run (
    id VARCHAR(64) PRIMARY KEY,
    uid VARCHAR(32) NOT NULL,
    theme_id VARCHAR(64) NOT NULL,
    start_ts BIGINT,
    record_json CLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_roguelike_run_uid_theme ON roguelike_run (uid, theme_id);

