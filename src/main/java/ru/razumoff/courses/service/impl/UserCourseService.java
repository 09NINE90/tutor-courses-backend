package ru.razumoff.courses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.razumoff.courses.dao.dto.CourseRsDto;
import ru.razumoff.courses.dao.dto.DashboardResponse;
import ru.razumoff.courses.dao.entity.CourseEnrollmentEntity;
import ru.razumoff.courses.dao.entity.CourseEntity;
import ru.razumoff.courses.dao.repository.CourseEnrollmentRepository;
import ru.razumoff.courses.dao.repository.CourseRepository;
import ru.razumoff.courses.service.IUserCourseService;
import ru.razumoff.exceptions.ErrorCode;
import ru.razumoff.exceptions.PlatformException;
import ru.razumoff.jwt.JwtUserPrincipal;
import ru.razumoff.minio.IMinioFileService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCourseService implements IUserCourseService {

    private final CourseRepository repository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final IMinioFileService minioService;

    /**
     * Дашборд курсов: tutor=свои, student=активные
     */
    @Override
    public DashboardResponse getCoursesDashboard(JwtUserPrincipal principal, int pageNumber, int pageSize, String sortBy) {
        Sort sort = getSortBy(sortBy);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);

        DashboardResponse response;
        if (principal.hasAnyPermission("COURSE_READ_OWN")) {
            response = getAllCoursesByOwner(principal.getId(), pageRequest);
        } else if (principal.hasAnyPermission("COURSE_READ_ENROLLED")) {
            response = getStudentCourses(principal.getId(), pageRequest);
        } else {
            throw new PlatformException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        return response;
    }

    private Sort getSortBy(String sortBy) {
        if (sortBy.equals("lastViewDesc")) {
            return Sort.by(Sort.Direction.DESC, "lastViewAt");
        } else {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    /**
     * Курсы владельца с пагинацией
     */
    private DashboardResponse getAllCoursesByOwner(UUID userId, PageRequest pageRequest) {
        Page<CourseEntity> courses = repository.findAllByOwnerId(userId, pageRequest);

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
                .pageSize(pageRequest.getPageSize())
                .currentPage(pageRequest.getPageNumber())
                .totalPages(courses.getTotalPages())
                .totalElements(courses.getTotalElements())
                .courses(result)
                .build();
    }

    /**
     * Курсы студента по enrollment с пагинацией
     */
    private DashboardResponse getStudentCourses(UUID studentId, PageRequest pageRequest) {
        Page<CourseEnrollmentEntity> enrollmentsPage = enrollmentRepository.findAllByUserIdWithCourse(studentId, pageRequest);

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
                .pageSize(pageRequest.getPageSize())
                .currentPage(pageRequest.getPageNumber())
                .totalPages(enrollmentsPage.getTotalPages())
                .totalElements(enrollmentsPage.getTotalElements())
                .courses(courses)
                .build();
    }

}
