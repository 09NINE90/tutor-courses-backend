package ru.razumoff.mapper;

import org.springframework.stereotype.Component;
import ru.razumoff.courses.dao.dto.CourseMemberRsDto;
import ru.razumoff.courses.dao.entity.CourseEnrollmentEntity;
import ru.razumoff.dto.integration.ProfileRsDto;

@Component
public class CourseMapper {

    public CourseMemberRsDto toCourseMemberRsDto(CourseEnrollmentEntity enrollment,
                                                 ProfileRsDto profile) {
        return CourseMemberRsDto.builder()
                .userId(enrollment.getUserId())
                .email(profile != null ? profile.getEmail() : null)
                .firstName(profile != null ? profile.getFirstName() : null)
                .lastName(profile != null ? profile.getLastName() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .enrollmentStatus(enrollment.getStatus())
                .statusLabel(enrollment.getStatus().getLabel())
                .build();
    }

}
