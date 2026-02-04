ALTER TABLE course_enrollments
    ADD COLUMN invited_by UUID;

COMMENT
ON COLUMN course_enrollments.invited_by IS 'ID пользователя, который пригласил в курс';