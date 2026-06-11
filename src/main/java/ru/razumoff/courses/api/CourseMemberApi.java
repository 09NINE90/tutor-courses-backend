package ru.razumoff.courses.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.razumoff.annotation.LogExecution;
import ru.razumoff.courses.dao.dto.CourseMemberRsDto;
import ru.razumoff.courses.dao.dto.internal.InviteUserDto;
import ru.razumoff.courses.service.ICourseMemberService;
import ru.razumoff.jwt.JwtUserPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/members")
@Tag(name = "Course Members", description = "Управление участниками курса")
public class CourseMemberApi {

    private final ICourseMemberService service;

    @GetMapping
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @Operation(summary = "Получить участников курса по ID")
    public ResponseEntity<List<CourseMemberRsDto>> getMembers(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                              @PathVariable UUID courseId) {
        return ResponseEntity.ok(service.getMembers(principal, courseId));
    }

    @PostMapping("/invites")
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @Operation(summary = "Пригласить пользователя в курс")
    @PreAuthorize("#principal.requirePermission('COURSE_INVITE_SEND')")
    public ResponseEntity<Void> inviteUser(@AuthenticationPrincipal JwtUserPrincipal principal,
                                           @PathVariable UUID courseId,
                                           @RequestPart("id") String userId) {
        service.inviteUser(InviteUserDto.builder()
                .ownerId(principal.getId())
                .userId(UUID.fromString(userId))
                .courseId(courseId)
                .build()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invites/confirm")
    @LogExecution(level = LogExecution.LogLevel.INFO)
    @Operation(summary = "Принять приглашение в курс")
    public ResponseEntity<Void> confirmInvite(@AuthenticationPrincipal JwtUserPrincipal principal,
                                              @PathVariable UUID courseId) {
        service.confirmInvite(principal, courseId);
        return ResponseEntity.ok().build();
    }
}

