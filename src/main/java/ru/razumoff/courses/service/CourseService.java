package ru.razumoff.courses.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.commonlib.dto.integration.ProfileRsDto;
import ru.razumoff.commonlib.exceptions.ErrorCode;
import ru.razumoff.commonlib.exceptions.PlatformException;
import ru.razumoff.config.security.JwtUserPrincipal;
import ru.razumoff.courses.dao.dto.*;
import ru.razumoff.courses.dao.dto.internal.InviteUserDto;
import ru.razumoff.courses.dao.entity.CourseEnrollmentEntity;
import ru.razumoff.courses.dao.entity.CourseEntity;
import ru.razumoff.courses.dao.enumz.EnrollmentStatus;
import ru.razumoff.courses.dao.repository.CourseEnrollmentRepository;
import ru.razumoff.courses.dao.repository.CourseRepository;
import ru.razumoff.integretion.users.IUserIntegrationService;
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

        if (principal.hasRole("TUTOR")) {
            return getAllCoursesByOwner(principal.getId(), pageable);
        } else if (principal.hasRole("STUDENT")) {
            return getStudentCourses(principal.getId(), pageable);
        } else {
            throw new PlatformException(ErrorCode.AUTH_ACCESS_DENIED);
        }
    }

    /**
     * Курсы владельца (TUTOR) с пагинацией
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
                                .imageUrl(entity.getImageUrl())
                                .build()
                );
            }
        }

        return DashboardResponse.builder()
                .pageSize(pageable.getPageSize())
                .currentPage(pageable.getPageNumber())
                .totalPages(courses.getTotalPages())
                .totalElements(courses.getTotalElements())
                .isTutor(true)
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
                        .imageUrl(enrollment.getCourse().getImageUrl())
                        .build()
                )
                .toList();

        return DashboardResponse.builder()
                .pageSize(pageable.getPageSize())
                .currentPage(pageable.getPageNumber())
                .totalPages(enrollmentsPage.getTotalPages())
                .totalElements(enrollmentsPage.getTotalElements())
                .isTutor(false)
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

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = minioService.uploadCourseImage(image);
        }

        CourseEntity entity = new CourseEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setImageUrl(imageUrl);
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
        boolean isTutor = principal.hasRole("TUTOR");
        String status = null;
        if (principal.hasRole("TUTOR")) {
            entity = repository.findByOwnerIdAndId(userId, courseId).orElseThrow(
                    () -> new PlatformException(ErrorCode.COURSE_NOT_FOUND)
            );
        } else if (principal.hasRole("STUDENT")) {
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
                .imageUrl(entity.getImageUrl())
                .build();

        return CoursePageRsDto.builder()
                .isTutor(isTutor)
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

                    if (!principal.hasRole("TUTOR")) {
                        dto.setMe(enrollment.getUserId().equals(principal.getId()));
                    }

                    return dto;
                })
                .toList();
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
        principal.requireRole("STUDENT");

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
