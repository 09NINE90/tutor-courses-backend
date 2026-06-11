package ru.razumoff.courses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.courses.dao.dto.*;
import ru.razumoff.courses.dao.entity.CourseEnrollmentEntity;
import ru.razumoff.courses.dao.entity.CourseEntity;
import ru.razumoff.courses.dao.enumz.EnrollmentStatus;
import ru.razumoff.courses.dao.repository.CourseEnrollmentRepository;
import ru.razumoff.courses.dao.repository.CourseRepository;
import ru.razumoff.courses.service.ICourseService;
import ru.razumoff.exceptions.ErrorCode;
import ru.razumoff.exceptions.PlatformException;
import ru.razumoff.jwt.JwtUserPrincipal;
import ru.razumoff.minio.IMinioFileService;
import ru.razumoff.utils.DtoUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {

    private final CourseRepository repository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final IMinioFileService minioService;

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
        entity.setTitle(request.getTitle().trim());
        entity.setDescription(request.getDescription().trim());
        entity.setImageS3Key(s3Key);
        entity.setOwnerId(userId);

        repository.save(entity);
    }

    /**
     * Курс по ID + статус enrollment для STUDENT
     */
    @Override
    @Transactional(readOnly = true)
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
                .status(status)
                .course(course)
                .build();
    }

    /**
     * Отмечает курс как просмотренный текущим пользователем.
     *
     * @param principal аутентифицированный пользователь с JWT токеном
     * @param courseId  идентификатор курса для отметки просмотра
     * @throws PlatformException если:
     *                           <ul>
     *                               <li>курс не найден ({@link ErrorCode#COURSE_NOT_FOUND})</li>
     *                               <li>запись о зачислении не найдена ({@link ErrorCode#ENROLLMENT_NOT_FOUND})</li>
     *                           </ul>
     */
    @Override
    @Transactional
    public void viewCourse(JwtUserPrincipal principal, UUID courseId) {
        CourseEntity course = repository.findById(courseId)
                .orElseThrow(() -> new PlatformException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getOwnerId().equals(principal.getId())) {
            course.setLastViewAt(OffsetDateTime.now(ZoneOffset.UTC));
            repository.save(course);
        } else {
            CourseEnrollmentEntity enrollment = enrollmentRepository.findByCourseIdAndUserId(
                    courseId,
                    principal.getId()
            ).orElseThrow(() -> new PlatformException(ErrorCode.ENROLLMENT_NOT_FOUND));

            enrollment.setLastViewAt(OffsetDateTime.now(ZoneOffset.UTC));
            enrollmentRepository.save(enrollment);
        }
    }

    @Override
    @Transactional
    public CourseRsDto updateCourse(UUID courseId, CourseUpdateRequest request, JwtUserPrincipal principal) {
        CourseEntity course = repository.findById(courseId)
                .orElseThrow(() -> new PlatformException(ErrorCode.COURSE_NOT_FOUND));

        if (!principal.getId().equals(course.getOwnerId())) {
            throw new RuntimeException();
        }

        if (!DtoUtils.isDeepEmpty(request, CourseUpdateRequest::getTitle)) {
            course.setTitle(request.getTitle().trim());
        }

        if (!DtoUtils.isDeepEmpty(request, CourseUpdateRequest::getDescription)) {
            course.setDescription(request.getDescription().trim());
        }

        if (request.getDeleteImage()) {
            if (course.getImageS3Key() != null) {
                minioService.deleteImage(course.getImageS3Key());
                course.setImageS3Key(null);
            }
        }

        CourseEntity savedCourse = repository.save(course);

        return CourseRsDto.builder()
                .id(savedCourse.getId())
                .title(savedCourse.getTitle())
                .description(savedCourse.getDescription())
                .imageUrl(minioService.generatePublicUrl(savedCourse.getImageS3Key()))
                .build();
    }

    @Override
    @Transactional
    public CourseRsDto updateCourseImage(UUID courseId, MultipartFile image, JwtUserPrincipal principal) {
        CourseEntity course = repository.findById(courseId)
                .orElseThrow(() -> new PlatformException(ErrorCode.COURSE_NOT_FOUND));

        if (!principal.getId().equals(course.getOwnerId())) {
            throw new RuntimeException();
        }

        if (image != null && !image.isEmpty()) {
            if (course.getImageS3Key() != null) {
                minioService.deleteImage(course.getImageS3Key());
            }
            String s3Key = minioService.uploadCourseImage(image);
            course.setImageS3Key(s3Key);
        }

        CourseEntity savedCourse = repository.save(course);

        return CourseRsDto.builder()
                .id(savedCourse.getId())
                .title(savedCourse.getTitle())
                .description(savedCourse.getDescription())
                .imageUrl(minioService.generatePublicUrl(savedCourse.getImageS3Key()))
                .build();
    }


}
