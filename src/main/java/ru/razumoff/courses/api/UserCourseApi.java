package ru.razumoff.courses.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.razumoff.annotation.LogExecution;
import ru.razumoff.courses.dao.dto.DashboardResponse;
import ru.razumoff.courses.service.IUserCourseService;
import ru.razumoff.jwt.JwtUserPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/users")
@Tag(name = "User Courses", description = "Курсы текущего пользователя")
public class UserCourseApi {

    private final IUserCourseService service;

    @GetMapping("/dashboard")
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @Operation(summary = "Получить список курсов пользователя")
    public ResponseEntity<DashboardResponse> getAllCourses(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                           @RequestParam(name = "page_number", defaultValue = "0") int pageNumber,
                                                           @RequestParam(name = "page_size", defaultValue = "12") int pageSize,
                                                           @RequestParam(name = "sort", defaultValue = "lastViewDesc") String sortBy) {
        return ResponseEntity.ok(service.getCoursesDashboard(principal, pageNumber, pageSize, sortBy));
    }
}
