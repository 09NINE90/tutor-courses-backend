package ru.razumoff.courses.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.razumoff.courses.dao.entity.CourseInviteLinkEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseInviteLinkRepository extends JpaRepository<CourseInviteLinkEntity, UUID> {

    @Query("SELECT il FROM CourseInviteLinkEntity il WHERE il.course.id = :courseId AND il.isActive = true ORDER BY il.createdAt DESC")
    Optional<CourseInviteLinkEntity> findFirstByCourseIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("courseId") UUID courseId);

    @Query("""
            SELECT il FROM CourseInviteLinkEntity il 
            WHERE il.token = :token AND il.course.id = :courseId 
            AND il.isActive = true
            """)
    Optional<CourseInviteLinkEntity> findByTokenAndCourseId(
            @Param("token") UUID token,
            @Param("courseId") UUID courseId
    );
}
