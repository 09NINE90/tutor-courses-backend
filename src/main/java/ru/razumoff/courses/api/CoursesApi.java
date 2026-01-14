package ru.razumoff.courses.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.config.security.JwtUserPrincipal;
import ru.razumoff.courses.dao.dto.CourseRsDto;
import ru.razumoff.courses.dao.dto.CreateCourseRqDto;
import ru.razumoff.courses.service.ICourseService;

import java.util.List;
import java.util.UUID;

import static ru.razumoff.Constants.ApiDocs.COURSES_TAG_DESCRIPTION;
import static ru.razumoff.Constants.ApiDocs.COURSES_TAG_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
@Tag(name = COURSES_TAG_NAME, description = COURSES_TAG_DESCRIPTION)
public class CoursesApi {

    private final ICourseService service;

    @GetMapping("/dashboard")
    @Operation(summary = "Получить список курсов пользователя")
    public ResponseEntity<List<CourseRsDto>> getAllCourses(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(service.getAllCoursesByUser(principal));
    }

    @PostMapping("/create")
    @Operation(summary = "Создать курс")
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

    @GetMapping("/{course_id}")
    @Operation(summary = "Получить данные о курсе по ID")
    public ResponseEntity<CourseRsDto> getCourseById(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                     @PathVariable("course_id") UUID courseId) {
        return ResponseEntity.ok(service.getCourseById(principal, courseId));
    }

}
