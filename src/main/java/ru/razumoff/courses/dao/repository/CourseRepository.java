package ru.razumoff.courses.dao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.razumoff.courses.dao.entity.CourseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {
    Page<CourseEntity> findAllByOwnerId(UUID ownerId, Pageable pageRequest);

    Page<CourseEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);

    Optional<CourseEntity> findByOwnerIdAndId(UUID ownerId, UUID courseId);

    @Query("SELECT c FROM CourseEntity c WHERE c.id IN :coursesIds")
    List<CourseEntity> findAllById(List<UUID> coursesIds);
}
