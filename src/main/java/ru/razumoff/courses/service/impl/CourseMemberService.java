package ru.razumoff.courses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.razumoff.courses.dao.dto.CourseMemberRsDto;
import ru.razumoff.courses.dao.dto.internal.InviteUserDto;
import ru.razumoff.courses.dao.entity.CourseEnrollmentEntity;
import ru.razumoff.courses.dao.entity.CourseEntity;
import ru.razumoff.courses.dao.enumz.EnrollmentStatus;
import ru.razumoff.courses.dao.repository.CourseEnrollmentRepository;
import ru.razumoff.courses.dao.repository.CourseRepository;
import ru.razumoff.courses.service.ICourseMemberService;
import ru.razumoff.dto.integration.ProfileRsDto;
import ru.razumoff.exceptions.ErrorCode;
import ru.razumoff.exceptions.PlatformException;
import ru.razumoff.integretion.users.IUserIntegrationService;
import ru.razumoff.jwt.JwtUserPrincipal;
import ru.razumoff.mapper.CourseMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseMemberService implements ICourseMemberService {

    private final CourseRepository repository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final IUserIntegrationService userIntegrationService;
    private final CourseMapper courseMapper;

    /**
     * Участники курса с профилями + isMe для STUDENT
     */
    @Override
    public List<CourseMemberRsDto> getMembers(JwtUserPrincipal principal, UUID courseId) {
        List<CourseEnrollmentEntity> enrollments = enrollmentRepository.findAllByCourseId(courseId);

        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> userIds = enrollments.stream()
                .map(CourseEnrollmentEntity::getUserId)
                .distinct()
                .toList();

        List<ProfileRsDto> profiles = userIntegrationService.getUserProfiles(userIds);

        Map<UUID, ProfileRsDto> profileMap = profiles.stream()
                .collect(Collectors.toMap(ProfileRsDto::getId, Function.identity()));

        return enrollments.stream()
                .map(enrollment -> {
                    ProfileRsDto profile = profileMap.get(enrollment.getUserId());
                    CourseMemberRsDto dto = courseMapper.toCourseMemberRsDto(enrollment, profile);

                    dto.setMe(enrollment.getUserId().equals(principal.getId()));

                    return dto;
                })
                .sorted((dto1, dto2) -> {
                    int lastNameCompare = compareNullableStrings(dto1.getLastName(), dto2.getLastName());
                    if (lastNameCompare != 0) {
                        return lastNameCompare;
                    }
                    return compareNullableStrings(dto1.getFirstName(), dto2.getFirstName());
                })
                .toList();
    }

    /**
     * Сравнение двух строк с учетом null значений
     * null значения идут в конец
     */
    private int compareNullableStrings(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return 0;
        }
        if (str1 == null) {
            return 1;
        }
        if (str2 == null) {
            return -1;
        }
        return str1.compareToIgnoreCase(str2);
    }

    /**
     * Пригласить пользователя в курс (INVITED статус)
     */
    @Override
    @Transactional
    public void inviteUser(InviteUserDto request) {
        CourseEntity course = repository.findById(request.getCourseId()).orElseThrow(
                () -> new PlatformException(ErrorCode.COURSE_NOT_FOUND)
        );

        CourseEnrollmentEntity enrollment = CourseEnrollmentEntity.builder()
                .invitedBy(request.getOwnerId())
                .userId(request.getUserId())
                .course(course)
                .status(EnrollmentStatus.INVITED)
                .build();

        enrollmentRepository.save(enrollment);
    }

    /**
     * Подтвердить приглашение STUDENT → ACTIVE
     */
    @Override
    @Transactional
    public void confirmInvite(JwtUserPrincipal principal, UUID courseId) {

        CourseEntity course = repository.findById(courseId).orElseThrow(
                () -> new PlatformException(ErrorCode.COURSE_NOT_FOUND)
        );

        CourseEnrollmentEntity enrollment = enrollmentRepository.findByUserIdAndCourse(
                principal.getId(),
                course
        );

        EnrollmentStatus currentStatus = enrollment.getStatus();

        if (currentStatus.equals(EnrollmentStatus.BLOCKED) ||
                currentStatus.equals(EnrollmentStatus.DROPPED) ||
                currentStatus.equals(EnrollmentStatus.SUSPENDED)) {
            throw new PlatformException(ErrorCode.ENROLLMENT_CANNOT_ACTIVATE);
        }

        if (currentStatus.equals(EnrollmentStatus.ACTIVE)) {
            return;
        }

        if (currentStatus.equals(EnrollmentStatus.INVITED)) {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setEnrolledAt(OffsetDateTime.now(ZoneOffset.UTC));
            enrollment.setLastViewAt(OffsetDateTime.now(ZoneOffset.UTC));
            enrollmentRepository.save(enrollment);
        }
    }
}
