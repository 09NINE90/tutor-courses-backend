package ru.razumoff.courses.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.commonlib.dto.integration.ProfileRsDto;
import ru.razumoff.commonlib.exceptions.ErrorCode;
import ru.razumoff.commonlib.exceptions.PlatformException;
import ru.razumoff.config.security.JwtUserPrincipal;
import ru.razumoff.courses.dao.dto.*;
import ru.razumoff.courses.dao.entity.CourseEnrollmentEntity;
import ru.razumoff.courses.dao.entity.CourseEntity;
import ru.razumoff.courses.dao.repository.CourseEnrollmentRepository;
import ru.razumoff.courses.dao.repository.CourseRepository;
import ru.razumoff.integretion.users.IUserIntegrationService;
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

    @Override
    public DashboardResponse getCoursesDashboard(JwtUserPrincipal principal) {
        if (principal.hasRole("TUTOR")) {
            return getAllCoursesByOwner(principal.getId());
        } else if (principal.hasRole("STUDENT")) {
            return getStudentCourses(principal.getId());
        } else {
            throw new PlatformException(ErrorCode.AUTH_ACCESS_DENIED);
        }
    }

    private DashboardResponse getAllCoursesByOwner(UUID userId) {
        List<CourseEntity> entities = repository.findAllByOwnerIdOrderByCreatedAtDesc(userId);

        List<CourseRsDto> result = new ArrayList<>();
        if (!entities.isEmpty()) {
            for (CourseEntity entity : entities) {
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
                .isTutor(true)
                .courses(result)
                .build();
    }

    private DashboardResponse getStudentCourses(UUID studentId) {
        List<CourseEnrollmentEntity> enrollments = enrollmentRepository.findAllByUserId(studentId);

        List<CourseRsDto> result = new ArrayList<>();
        if (!enrollments.isEmpty()) {
            List<UUID> coursesIds = enrollments.stream()
                    .map(CourseEnrollmentEntity::getCourse)
                    .map(CourseEntity::getId)
                    .toList();

            List<CourseEntity> courses = repository.findAllById(coursesIds);
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
                .isTutor(false)
                .courses(result)
                .build();
    }

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

    @Override
    public CourseRsDto getCourseById(JwtUserPrincipal principal, UUID courseId) {
        UUID userId = principal.getId();

        CourseEntity entity;
        if (principal.hasRole("TUTOR")) {
            entity = repository.findByOwnerIdAndId(userId, courseId).orElseThrow(
                    () -> new PlatformException(ErrorCode.COURSE_NOT_FOUND)
            );
        } else if (principal.hasRole("STUDENT")) {
            entity = repository.findById(courseId).orElseThrow(
                    () -> new PlatformException(ErrorCode.COURSE_NOT_FOUND)
            );
        } else {
            throw new PlatformException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        return CourseRsDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .build();
    }

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
                    return CourseMemberRsDto.builder()
                            .userId(enrollment.getUserId())
                            .email(profile != null ? profile.getEmail() : null)
                            .firstName(profile != null ? profile.getFirstName() : null)
                            .lastName(profile != null ? profile.getLastName() : null)
                            .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                            .enrollmentStatus(enrollment.getStatus())
                            .statusLabel(enrollment.getStatus().getLabel())
                            .build();
                })
                .toList();
    }
}
