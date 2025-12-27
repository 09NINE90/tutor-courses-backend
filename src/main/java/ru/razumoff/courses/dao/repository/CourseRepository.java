package ru.razumoff.courses.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.razumoff.courses.dao.entity.CourseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {
    List<CourseEntity> findAllByOwnerId(UUID ownerId);

    List<CourseEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    Optional<CourseEntity> findByOwnerIdAndId(UUID ownerId, UUID courseId);
}
