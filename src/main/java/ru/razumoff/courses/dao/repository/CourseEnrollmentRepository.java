package ru.razumoff.courses.dao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.razumoff.courses.dao.entity.CourseEnrollmentEntity;
import ru.razumoff.courses.dao.entity.CourseEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollmentEntity, UUID> {
    List<CourseEnrollmentEntity> findAllByCourseId(UUID courseId);

    List<CourseEnrollmentEntity> findAllByUserId(UUID studentId, Pageable pageable);

    @Query("SELECT e FROM CourseEnrollmentEntity e " +
            "JOIN FETCH e.course c " +
            "WHERE e.userId = :studentId")
    Page<CourseEnrollmentEntity> findAllByUserIdWithCourse(
            @Param("studentId") UUID studentId,
            Pageable pageable
    );

    CourseEnrollmentEntity findByUserIdAndCourse(UUID studentId, CourseEntity course);

    Optional<CourseEnrollmentEntity> findByCourseIdAndUserId(UUID courseId, UUID studentId);

    @Query(value = """
        INSERT INTO courses_service.course_enrollments (course_id, user_id, status, enrolled_at, invited_by, created_at, updated_at)
        VALUES (:courseId, :userId, 'ACTIVE', :enrolledAt, :invitedBy, NOW(), NOW())
        ON CONFLICT (course_id, user_id) 
        DO UPDATE SET 
            status = 'ACTIVE', 
            enrolled_at = :enrolledAt, 
            invited_by = :invitedBy,
            updated_at = NOW()
        RETURNING id
        """, nativeQuery = true)
    UUID upsertEnrollment(
            @Param("courseId") UUID courseId,
            @Param("userId") UUID userId,
            @Param("enrolledAt") OffsetDateTime enrolledAt,
            @Param("invitedBy") UUID invitedBy
    );

}
