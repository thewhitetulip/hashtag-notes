DROP TABLE IF EXISTS note;

CREATE TABLE note (
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    content TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE (created_at)
);