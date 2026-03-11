package ru.razumoff.courses.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.courses.dao.dto.CourseMemberRsDto;
import ru.razumoff.courses.dao.dto.CoursePageRsDto;
import ru.razumoff.courses.dao.dto.CreateCourseRqDto;
import ru.razumoff.courses.dao.dto.internal.InviteUserDto;
import ru.razumoff.courses.service.ICourseService;
import ru.razumoff.jwt.JwtUserPrincipal;

import java.util.List;
import java.util.UUID;

import static ru.razumoff.Constants.ApiDocs.COURSE_TAG_DESCRIPTION;
import static ru.razumoff.Constants.ApiDocs.COURSE_TAG_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
@Tag(name = COURSE_TAG_NAME, description = COURSE_TAG_DESCRIPTION)
public class CourseApi {

    private final ICourseService service;

    @PostMapping("/create")
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

    @GetMapping("/{course_id}")
    @Operation(summary = "Получить данные о курсе по ID")
    public ResponseEntity<CoursePageRsDto> getCourseById(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                         @PathVariable("course_id") UUID courseId) {
        return ResponseEntity.ok(service.getCourseById(principal, courseId));
    }

    @PutMapping("/{course_id}")
    @Operation(summary = "Обновить дату просмотра курса")
    public ResponseEntity<Void> viewCourse(@AuthenticationPrincipal JwtUserPrincipal principal,
                                           @PathVariable("course_id") UUID courseId) {
        service.viewCourse(principal, courseId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/{course_id}/members")
    @Operation(summary = "Получить участников курса по ID")
    public ResponseEntity<List<CourseMemberRsDto>> getCourseMembersById(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                                        @PathVariable("course_id") UUID courseId) {
        return ResponseEntity.ok(service.getCourseMembersById(principal, courseId));
    }

    @PostMapping("/{course_id}/invite")
    @Operation(summary = "Пригласить пользователя в курс")
    @PreAuthorize("#principal.requirePermission('COURSE_INVITE_SEND')")
    public ResponseEntity<Void> inviteUserToCourse(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                   @PathVariable("course_id") UUID courseId,
                                                   @RequestPart("id") String userId) {
        service.inviteUserToCourse(InviteUserDto.builder()
                .ownerId(principal.getId())
                .userId(UUID.fromString(userId))
                .courseId(courseId)
                .build()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{course_id}/invite/confirm")
    @Operation(summary = "Принять приглашение в курс")
    public ResponseEntity<Void> confirmInvite(@AuthenticationPrincipal JwtUserPrincipal principal,
                                              @PathVariable("course_id") UUID courseId) {
        service.confirmInvite(principal, courseId);
        return ResponseEntity.ok().build();
    }
}
