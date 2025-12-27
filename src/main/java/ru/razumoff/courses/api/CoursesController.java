package ru.razumoff.courses.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.razumoff.config.security.JwtUserPrincipal;
import ru.razumoff.courses.dao.dto.CourseRsDto;
import ru.razumoff.courses.dao.dto.CreateCourseRqDto;
import ru.razumoff.courses.service.ICourseService;
import ru.razumoff.integretion.IUserIntegrationService;

import java.util.List;
import java.util.UUID;

import static ru.razumoff.Constants.ApiDocs.COURSES_TAG_DESCRIPTION;
import static ru.razumoff.Constants.ApiDocs.COURSES_TAG_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
@Tag(name = COURSES_TAG_NAME, description = COURSES_TAG_DESCRIPTION)
public class CoursesController {

    private final IUserIntegrationService authClient;
    private final ICourseService service;

    @GetMapping
    @Operation(summary = "Получить список курсов пользователя")
    public ResponseEntity<List<CourseRsDto>> getAllCourses(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(service.getAllCoursesByUser(principal));
    }

    @PostMapping("/create")
    @Operation(summary = "Создать курс")
    public ResponseEntity<Void> createCourse(@AuthenticationPrincipal JwtUserPrincipal principal,
                                             @RequestBody CreateCourseRqDto request) {
        service.createCourse(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{course_id}")
    @Operation(summary = "Получить данные о курсе по ID")
    public ResponseEntity<CourseRsDto> getCourseById(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                     @PathVariable("course_id") UUID courseId) {
        return ResponseEntity.ok(service.getCourseById(principal, courseId));
    }

    //    @GetMapping("/profile")
//    @PreAuthorize("#principal.requireRole('ADMIN')")
//    public ResponseEntity<ProfileRsDto> profile(@AuthenticationPrincipal JwtUserPrincipal principal) {
//        ProfileRsDto profileUser = authClient.getUserProfile(principal.getId());
//        return ResponseEntity.ok(profileUser);
//    }
}
