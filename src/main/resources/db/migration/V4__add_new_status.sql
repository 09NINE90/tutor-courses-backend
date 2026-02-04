ALTER TABLE course_enrollments
DROP
CONSTRAINT IF EXISTS course_enrollments_status_check;

ALTER TABLE course_enrollments
    ADD CONSTRAINT course_enrollments_status_check
        CHECK (
            status IN (
                       'INVITED',
                       'ACTIVE',
                       'SUSPENDED',
                       'BLOCKED',
                       'DROPPED',
                       'NO_ACCESS'
                )
            );

COMMENT
ON COLUMN course_enrollments.status
IS 'Статус: INVITED, ACTIVE, SUSPENDED, BLOCKED, DROPPED, NO_ACCESS';