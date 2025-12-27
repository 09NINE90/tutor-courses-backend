CREATE TABLE courses
(
    id          UUID PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    image_url   TEXT                  DEFAULT NULL,
    owner_id    UUID         NOT NULL,
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Опционально: ограничить статус
ALTER TABLE courses
    ADD CONSTRAINT chk_courses_status
        CHECK (status IN ('ACTIVE', 'ARCHIVED'));
