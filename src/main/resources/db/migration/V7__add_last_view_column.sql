ALTER TABLE courses
    ADD COLUMN last_view_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE course_enrollments
    ADD COLUMN last_view_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

COMMENT
    ON COLUMN course_enrollments.last_view_at IS 'Дата и время последнего открытия курса';

COMMENT
    ON COLUMN courses.last_view_at IS 'Дата и время последнего открытия курса';