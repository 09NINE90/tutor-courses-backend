CREATE TABLE course_enrollments
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    course_id   UUID        NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL,
    enrolled_at TIMESTAMPTZ          DEFAULT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'INVITED' CHECK (
        status IN ('INVITED', 'ACTIVE', 'SUSPENDED', 'BLOCKED', 'DROPPED')
        ),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (course_id, user_id)
);

-- Индексы для быстрых запросов
CREATE INDEX idx_enrollments_course ON course_enrollments (course_id);
CREATE INDEX idx_enrollments_user ON course_enrollments (user_id);
CREATE INDEX idx_enrollments_course_status ON course_enrollments (course_id, status);

-- Комментарии
COMMENT
ON TABLE course_enrollments IS 'Участники курса: many-to-many связь courses ↔ users со статусом участия';
COMMENT
ON COLUMN course_enrollments.course_id IS 'ID курса';
COMMENT
ON COLUMN course_enrollments.user_id IS 'ID пользователя';
COMMENT
ON COLUMN course_enrollments.enrolled_at IS 'Дата присоединения';
COMMENT
ON COLUMN course_enrollments.status IS 'Статус: ACTIVE, INVITED, BLOCKED';
COMMENT
ON COLUMN course_enrollments.created_at IS 'Дата и время создания записи участия в курсе';
COMMENT
ON COLUMN course_enrollments.updated_at IS 'Дата и время последнего изменения статуса/записи';
