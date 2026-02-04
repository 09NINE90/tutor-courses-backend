package ru.razumoff.courses.dao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.razumoff.courses.dao.entity.CourseEnrollmentEntity;
import ru.razumoff.courses.dao.entity.CourseEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollmentEntity, UUID> {
    List<CourseEnrollmentEntity> findAllByCourseId(UUID courseId);

    List<CourseEnrollmentEntity> findAllByUserId(UUID studentId, Pageable pageable);

    @Query("SELECT e FROM CourseEnrollmentEntity e " +
            "JOIN FETCH e.course c " +
            "WHERE e.userId = :studentId " +
            "ORDER BY e.enrolledAt DESC")
    Page<CourseEnrollmentEntity> findAllByUserIdWithCourse(
            @Param("studentId") UUID studentId,
            Pageable pageable
    );

    CourseEnrollmentEntity findByUserIdAndCourse(UUID studentId, CourseEntity course);

}
