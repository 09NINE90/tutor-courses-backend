package ru.razumoff.courses.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.courses.dao.dto.*;
import ru.razumoff.courses.dao.dto.internal.InviteUserDto;
import ru.razumoff.courses.dao.entity.CourseEnrollmentEntity;
import ru.razumoff.courses.dao.entity.CourseEntity;
import ru.razumoff.courses.dao.enumz.EnrollmentStatus;
import ru.razumoff.courses.dao.repository.CourseEnrollmentRepository;
import ru.razumoff.courses.dao.repository.CourseRepository;
import ru.razumoff.dto.integration.ProfileRsDto;
import ru.razumoff.exceptions.ErrorCode;
import ru.razumoff.exceptions.PlatformException;
import ru.razumoff.integretion.users.IUserIntegrationService;
import ru.razumoff.jwt.JwtUserPrincipal;
import ru.razumoff.mapper.CourseMapper;
import ru.razumoff.minio.IMinioFileService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {

    private final CourseRepository repository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final IMinioFileService minioService;
    private final IUserIntegrationService userIntegrationService;
    private final CourseMapper courseMapper;

    /**
     * Дашборд курсов: tutor=свои, student=активные
     */
    @Override
    public DashboardResponse getCoursesDashboard(JwtUserPrincipal principal, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        DashboardResponse response;
        if (principal.hasAnyPermission("COURSE_READ_OWN")) {
            response = getAllCoursesByOwner(principal.getId(), pageable);
        } else if (principal.hasAnyPermission("COURSE_READ_ENROLLED")) {
            response = getStudentCourses(principal.getId(), pageable);
        } else {
            throw new PlatformException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        response.setPermissions(principal.getPermissions());
        return response;
    }

    /**
     * Курсы владельца с пагинацией
     */
    private DashboardResponse getAllCoursesByOwner(UUID userId, Pageable pageable) {
        Page<CourseEntity> courses = repository.findAllByOwnerIdOrderByCreatedAtDesc(userId, pageable);

        List<CourseRsDto> result = new ArrayList<>();
        if (!courses.isEmpty()) {
            for (CourseEntity entity : courses) {
                result.add(
                        CourseRsDto.builder()
                                .id(entity.getId())
                                .title(entity.getTitle())
                                .description(entity.getDescription())
                                .imageUrl(minioService.generatePublicUrl(entity.getImageS3Key()))
                                .build()
                );
            }
        }

        return DashboardResponse.builder()
                .pageSize(pageable.getPageSize())
                .currentPage(pageable.getPageNumber())
                .totalPages(courses.getTotalPages())
                .totalElements(courses.getTotalElements())
                .courses(result)
                .build();
    }

    /**
     * Курсы студента по enrollment с пагинацией
     */
    private DashboardResponse getStudentCourses(UUID studentId, Pageable pageable) {
        Page<CourseEnrollmentEntity> enrollmentsPage = enrollmentRepository.findAllByUserIdWithCourse(studentId, pageable);

        List<CourseRsDto> courses = enrollmentsPage.getContent().stream()
                .map(enrollment -> CourseRsDto.builder()
                        .id(enrollment.getCourse().getId())
                        .title(enrollment.getCourse().getTitle())
                        .description(enrollment.getCourse().getDescription())
                        .imageUrl(minioService.generatePublicUrl(enrollment.getCourse().getImageS3Key()))
                        .build()
                )
                .toList();

        return DashboardResponse.builder()
                .pageSize(pageable.getPageSize())
                .currentPage(pageable.getPageNumber())
                .totalPages(enrollmentsPage.getTotalPages())
                .totalElements(enrollmentsPage.getTotalElements())
                .courses(courses)
                .build();
    }

    /**
     * Создать курс с аватаркой MinIO
     */
    @Override
    public void createCourse(JwtUserPrincipal principal,
                             CreateCourseRqDto request, MultipartFile image) {
        UUID userId = principal.getId();

        String s3Key = minioService.uploadCourseImage(image);

        CourseEntity entity = new CourseEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setImageS3Key(s3Key);
        entity.setOwnerId(userId);

        repository.save(entity);
    }

    /**
     * Курс по ID + статус enrollment для STUDENT
     */
    @Override
    public CoursePageRsDto getCourseById(JwtUserPrincipal principal, UUID courseId) {
        UUID userId = principal.getId();

        CourseEntity entity;
        String status = null;
        if (principal.hasAnyPermission("COURSE_READ_OWN")) {
            entity = repository.findByOwnerIdAndId(userId, courseId).orElseThrow(
                    () -> new PlatformException(ErrorCode.COURSE_NOT_FOUND)
            );
        } else if (principal.hasAnyPermission("COURSE_READ_ENROLLED")) {
            entity = repository.findById(courseId).orElseThrow(
                    () -> new PlatformException(ErrorCode.COURSE_NOT_FOUND)
            );
            CourseEnrollmentEntity enrollment = enrollmentRepository.findByUserIdAndCourse(userId, entity);
            if (enrollment == null) {
                status = EnrollmentStatus.NO_ACCESS.toString();
            } else {
                status = enrollment.getStatus().toString();
            }
        } else {
            throw new PlatformException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        CourseRsDto course = CourseRsDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageUrl(minioService.generatePublicUrl(entity.getImageS3Key()))
                .build();

        return CoursePageRsDto.builder()
                .permissions(principal.getPermissions())
                .status(status)
                .course(course)
                .build();
    }

    /**
     * Участники курса с профилями + isMe для STUDENT
     */
    @Override
    public List<CourseMemberRsDto> getCourseMembersById(JwtUserPrincipal principal, UUID courseId) {
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
    public void inviteUserToCourse(InviteUserDto request) {
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
            enrollmentRepository.save(enrollment);
        }
    }
}
