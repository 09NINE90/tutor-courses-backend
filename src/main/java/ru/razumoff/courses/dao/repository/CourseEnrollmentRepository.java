package ru.razumoff.courses.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.razumoff.courses.dao.entity.CourseEnrollmentEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollmentEntity, UUID> {
    List<CourseEnrollmentEntity> findAllByCourseId(UUID courseId);

    List<CourseEnrollmentEntity> findAllByUserId(UUID studentId);
}
