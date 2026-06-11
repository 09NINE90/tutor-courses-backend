package ru.razumoff.courses.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.annotation.LogExecution;
import ru.razumoff.courses.dao.dto.*;
import ru.razumoff.courses.service.ICourseService;
import ru.razumoff.jwt.JwtUserPrincipal;

import java.util.UUID;

import static ru.razumoff.Constants.ApiDocs.COURSE_TAG_DESCRIPTION;
import static ru.razumoff.Constants.ApiDocs.COURSE_TAG_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
@Tag(name = COURSE_TAG_NAME, description = COURSE_TAG_DESCRIPTION)
public class CourseApi {

    private final ICourseService service;

    @PostMapping
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @Operation(summary = "Создать курс")
    @PreAuthorize("#principal.requirePermission('COURSE_CREATE')")
    public ResponseEntity<Void> createCourse(@AuthenticationPrincipal JwtUserPrincipal principal,
                                             @RequestPart("title") String title,
                                             @RequestPart("description") String description,
                                             @RequestPart(value = "image", required = false) MultipartFile image) {
        service.createCourse(
                principal,
                CreateCourseRqDto.builder()
                        .title(title)
                        .description(description)
                        .build(),
                image
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{courseId}")
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @Operation(summary = "Получить данные о курсе по ID")
    public ResponseEntity<CoursePageRsDto> getCourseById(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                         @PathVariable UUID courseId) {
        return ResponseEntity.ok(service.getCourseById(principal, courseId));
    }

    @PutMapping("/{courseId}/view")
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @Operation(summary = "Обновить дату просмотра курса")
    public ResponseEntity<Void> viewCourse(@AuthenticationPrincipal JwtUserPrincipal principal,
                                           @PathVariable UUID courseId) {
        service.viewCourse(principal, courseId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PatchMapping("/{courseId}")
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @PreAuthorize("#principal.requirePermission('COURSE_UPDATE_OWN')")
    @Operation(summary = "Обновить курс", description = "Обновить название, описание курса.")
    public ResponseEntity<CourseRsDto> updateCourse(
            @Parameter(description = "Course ID", required = true)
            @PathVariable UUID courseId,
            @RequestBody CourseUpdateRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        CourseRsDto updatedCourse = service.updateCourse(courseId, request, principal);
        return ResponseEntity.ok(updatedCourse);
    }

    @PatchMapping("/{courseId}/image")
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @PreAuthorize("#principal.requirePermission('COURSE_UPDATE_OWN')")
    @Operation(summary = "Обновить курс", description = "Обновить изображение курса.")
    public ResponseEntity<CourseRsDto> updateCourseImage(
            @Parameter(description = "Course ID", required = true)
            @PathVariable UUID courseId,

            @Parameter(description = "Файл изображения курса")
            @RequestPart(value = "image", required = false)
            MultipartFile image,

            @AuthenticationPrincipal JwtUserPrincipal principal) {
        CourseRsDto updatedCourse = service.updateCourseImage(courseId, image, principal);
        return ResponseEntity.ok(updatedCourse);
    }
}
