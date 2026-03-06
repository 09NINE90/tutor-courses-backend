CREATE TABLE course_invite_links
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id  UUID         NOT NULL REFERENCES courses ON DELETE CASCADE,
    token      UUID         NOT NULL UNIQUE,
    max_uses   INTEGER          DEFAULT NULL,
    uses_count INTEGER          DEFAULT 0 CHECK (uses_count <= max_uses),
    expires_at TIMESTAMPTZ  NOT NULL,
    created_by UUID         NOT NULL,
    created_at TIMESTAMPTZ      DEFAULT NOW(),
    is_active  BOOLEAN          DEFAULT true,

    UNIQUE (course_id, token)
);

CREATE INDEX idx_invite_links_course_active ON course_invite_links (course_id, is_active);
CREATE INDEX idx_invite_links_token ON course_invite_links (token);

COMMENT ON TABLE course_invite_links IS 'Пригласительные ссылки на курсы с лимитами';
