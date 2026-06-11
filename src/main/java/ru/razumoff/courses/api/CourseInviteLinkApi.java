package ru.razumoff.courses.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.razumoff.annotation.LogExecution;
import ru.razumoff.courses.dao.dto.CreateInviteLinkRqDto;
import ru.razumoff.courses.dao.dto.InviteLinkRsDto;
import ru.razumoff.courses.dao.dto.JoinCourseRqDto;
import ru.razumoff.courses.service.ICourseInviteLinkService;
import ru.razumoff.jwt.JwtUserPrincipal;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/invite-links")
@Tag(name = "Course Invite Links", description = "Управление ссылками-приглашениями")
public class CourseInviteLinkApi {

    private final ICourseInviteLinkService service;

    @PostMapping
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @Operation(summary = "Создать новую ссылку для приглашения в курс")
    public ResponseEntity<InviteLinkRsDto> createInviteLink(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                            @PathVariable UUID courseId,
                                                            @RequestBody CreateInviteLinkRqDto request) {
        return ResponseEntity.ok(service.createInviteLink(principal, request, courseId));
    }

    @GetMapping("/active")
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @Operation(summary = "Получить последнюю валидную ссылку для приглашения в курс")
    public ResponseEntity<InviteLinkRsDto> getInviteLinks(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                          @PathVariable UUID courseId) {
        InviteLinkRsDto result = service.getInviteLinks(principal, courseId);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }


    @PostMapping("/join")
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @Operation(summary = "Присоединиться к курсу по приглашению")
    public ResponseEntity<Void> joinCourse(@AuthenticationPrincipal JwtUserPrincipal principal,
                                           @PathVariable UUID courseId,
                                           @RequestBody JoinCourseRqDto request) {
        service.joinWithInvite(courseId, request, principal);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
