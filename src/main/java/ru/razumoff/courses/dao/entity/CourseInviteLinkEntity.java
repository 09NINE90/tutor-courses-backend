package ru.razumoff.courses.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.razumoff.courses.dao.enumz.CourseStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "course_invite_links")
public class CourseInviteLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Column(name = "token", nullable = false)
    private UUID token;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "uses_count", nullable = false)
    private Integer usesCount;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @PrePersist
    public void prePersist() {
        isActive = true;
        usesCount = 0;
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

}
