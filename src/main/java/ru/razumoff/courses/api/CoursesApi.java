package ru.razumoff.courses.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.razumoff.config.security.JwtUserPrincipal;
import ru.razumoff.courses.dao.dto.DashboardResponse;
import ru.razumoff.courses.service.ICourseService;

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
    public ResponseEntity<DashboardResponse> getAllCourses(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                           @RequestParam(name = "page_number", defaultValue = "0") int pageNumber,
                                                           @RequestParam(name = "page_size", defaultValue = "12") int pageSize) {
        return ResponseEntity.ok(service.getCoursesDashboard(principal, pageNumber, pageSize));
    }

}
