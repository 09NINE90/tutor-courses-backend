package ru.razumoff.courses.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.razumoff.courses.dao.dto.CreateInviteLinkRqDto;
import ru.razumoff.courses.dao.dto.DashboardResponse;
import ru.razumoff.courses.dao.dto.InviteLinkRsDto;
import ru.razumoff.courses.dao.dto.JoinCourseRqDto;
import ru.razumoff.courses.service.ICourseService;
import ru.razumoff.jwt.JwtUserPrincipal;

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
    public ResponseEntity<DashboardResponse> getAllCourses(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                           @RequestParam(name = "page_number", defaultValue = "0") int pageNumber,
                                                           @RequestParam(name = "page_size", defaultValue = "12") int pageSize) {
        return ResponseEntity.ok(service.getCoursesDashboard(principal, pageNumber, pageSize));
    }

    @PostMapping("/{course_id}/invite-links")
    @Operation(summary = "Создать новую ссылку для приглашения в курс")
    public ResponseEntity<InviteLinkRsDto> createInviteLink(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                            @PathVariable("course_id") UUID courseId,
                                                            @RequestBody CreateInviteLinkRqDto request) {
        return ResponseEntity.ok(service.createInviteLink(principal, request, courseId));
    }

    @GetMapping("/{course_id}/invite-links")
    @Operation(summary = "Получить последнюю валидную ссылку для приглашения в курс")
    public ResponseEntity<InviteLinkRsDto> getInviteLinks(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                          @PathVariable("course_id") UUID courseId) {
        InviteLinkRsDto result = service.getInviteLinks(principal, courseId);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }


    @PostMapping("/{courseId}/join")
    @Operation(summary = "Присоединиться к курсу по приглашению")
    public ResponseEntity<Void> joinCourse(@AuthenticationPrincipal JwtUserPrincipal principal,
                                        @PathVariable UUID courseId,
                                        @RequestBody JoinCourseRqDto request) {
        service.joinWithInvite(courseId, request, principal);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}
