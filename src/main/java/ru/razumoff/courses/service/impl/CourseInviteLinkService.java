package ru.razumoff.courses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.razumoff.courses.dao.dto.CreateInviteLinkRqDto;
import ru.razumoff.courses.dao.dto.InviteLinkRsDto;
import ru.razumoff.courses.dao.dto.JoinCourseRqDto;
import ru.razumoff.courses.dao.entity.CourseEntity;
import ru.razumoff.courses.dao.entity.CourseInviteLinkEntity;
import ru.razumoff.courses.dao.repository.CourseEnrollmentRepository;
import ru.razumoff.courses.dao.repository.CourseInviteLinkRepository;
import ru.razumoff.courses.dao.repository.CourseRepository;
import ru.razumoff.courses.service.ICourseInviteLinkService;
import ru.razumoff.exceptions.ErrorCode;
import ru.razumoff.exceptions.PlatformException;
import ru.razumoff.jwt.JwtUserPrincipal;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseInviteLinkService implements ICourseInviteLinkService {

    @Value("${origins.front}")
    private String FRONT_ORIGIN;

    private final CourseRepository repository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseInviteLinkRepository courseInviteLinkRepository;

    @Override
    @Transactional
    public InviteLinkRsDto createInviteLink(JwtUserPrincipal principal, CreateInviteLinkRqDto request, UUID courseId) {
        principal.requirePermission("COURSE_INVITE_SEND");

        CourseEntity course = repository.findById(courseId).orElseThrow(
                () -> new PlatformException(ErrorCode.COURSE_NOT_FOUND)
        );

        UUID inviteToken = UUID.randomUUID();
        OffsetDateTime expireDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(request.getExpiresDays());

        CourseInviteLinkEntity inviteLink = CourseInviteLinkEntity.builder()
                .course(course)
                .token(inviteToken)
                .expiresAt(expireDate)
                .createdBy(principal.getId())
                .maxUses(request.getMaxUses())
                .build();

        CourseInviteLinkEntity savedLink = courseInviteLinkRepository.save(inviteLink);

        String inviteUrl = String.format("%s/course/%s/invite/?token=%s",
                FRONT_ORIGIN, course.getId(), inviteToken);

        return InviteLinkRsDto.builder()
                .token(inviteToken)
                .usesCount(savedLink.getUsesCount())
                .maxUses(savedLink.getMaxUses())
                .inviteLink(inviteUrl)
                .expiresAt(expireDate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InviteLinkRsDto getInviteLinks(JwtUserPrincipal principal, UUID courseId) {
        CourseEntity course = repository.findById(courseId)
                .orElseThrow(() -> new PlatformException(ErrorCode.COURSE_NOT_FOUND));

        Optional<CourseInviteLinkEntity> latestLink = courseInviteLinkRepository
                .findFirstByCourseIdAndIsActiveTrueOrderByCreatedAtDesc(courseId);

        if (latestLink.isEmpty()) {
            return null;
        }

        CourseInviteLinkEntity link = latestLink.get();

        if (link.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            return null;
        }

        String inviteUrl = String.format("%s/course/%s/invite/?token=%s",
                FRONT_ORIGIN, course.getId(), link.getToken());

        return InviteLinkRsDto.builder()
                .token(link.getToken())
                .usesCount(link.getUsesCount())
                .maxUses(link.getMaxUses())
                .inviteLink(inviteUrl)
                .expiresAt(link.getExpiresAt())
                .build();
    }

    /**
     * Присоединяет студента к курсу по пригласительной ссылке.
     *
     * <h3>Логика работы:</h3>
     * <ol>
     *     <li>Валидация прав доступа (STUDENT)</li>
     *     <li>Поиск курса и пригласительной ссылки</li>
     *     <li>Проверка активности/срока действия ссылки</li>
     *     <li>Инкремент счётчика использований (атомарно)</li>
     *     <li>Обновление/создание enrollment (ACTIVE статус)</li>
     * </ol>
     *
     * <h3>Возвращаемые статусы:</h3>
     * <ul>
     *     <li><b>200 OK</b> — успешно присоединён (новый или обновлён)</li>
     *     <li><b>400</b> — {@link ErrorCode#INVITE_LINK_INVALID}, {@link ErrorCode#INVITE_LINK_EXPIRED}</li>
     *     <li><b>403</b> — нет прав COURSE_INVITE_CONFIRM</li>
     * </ul>
     *
     * @param courseId  ID курса
     * @param request   DTO с токеном ссылки
     * @param principal JWT пользователь (студент)
     * @throws PlatformException при ошибках валидации ссылки/прав
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinWithInvite(UUID courseId, JoinCourseRqDto request, JwtUserPrincipal principal) {
        UUID studentId = principal.getId();
        log.info("Join attempt: student={}, course={}, token={}",
                studentId, courseId, request.getToken());

        principal.requirePermission("COURSE_INVITE_CONFIRM");

        CourseEntity course = repository.findById(courseId)
                .orElseThrow(() -> new PlatformException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getOwnerId().equals(studentId)) {
            log.warn("Course owner {} tried to join own course {}", studentId, courseId);
            throw new PlatformException(ErrorCode.COURSE_OWNER_CANT_JOIN);
        }

        CourseInviteLinkEntity inviteLink = courseInviteLinkRepository
                .findByTokenAndCourseId(request.getToken(), courseId)
                .orElseThrow(() -> new PlatformException(ErrorCode.INVITE_LINK_INVALID));

        validateInviteLink(inviteLink);

        if (!incrementUsesCount(inviteLink)) {
            log.warn("Invite link limit exceeded: link={}, uses={}/{}",
                    inviteLink.getId(), inviteLink.getUsesCount(), inviteLink.getMaxUses());
            throw new PlatformException(ErrorCode.INVITE_LINK_EXCEEDED);
        }

        UUID invitedBy = inviteLink.getCreatedBy();
        log.debug("Invite from teacher: {}", invitedBy);

        enrollmentRepository.upsertEnrollment(course.getId(), studentId,
                OffsetDateTime.now(ZoneOffset.UTC), invitedBy);
        log.info("Enrollment upserted: student={} → course={}", studentId, courseId);

    }

    /**
     * Валидирует пригласительную ссылку перед использованием.
     *
     * @param inviteLink ссылка для проверки
     * @throws PlatformException если ссылка неактивна или просрочена
     */
    private void validateInviteLink(CourseInviteLinkEntity inviteLink) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (!inviteLink.isActive()) {
            log.warn("Invite link inactive: linkId={}, token={}",
                    inviteLink.getId(), inviteLink.getToken());
            throw new PlatformException(ErrorCode.INVITE_LINK_INACTIVE);
        }

        if (now.isAfter(inviteLink.getExpiresAt())) {
            log.warn("Invite link expired: linkId={}, expires={}, now={}",
                    inviteLink.getId(), inviteLink.getExpiresAt(), now);
            throw new PlatformException(ErrorCode.INVITE_LINK_EXPIRED);
        }

        log.debug("Invite link validated OK: expires={}", inviteLink.getExpiresAt());
    }

    /**
     * Атомарно инкрементит счётчик использований пригласительной ссылки.
     *
     * <p>Проверяет лимит maxUses (null = ∞). Деактивирует ссылку при превышении.</p>
     *
     * @param inviteLink ссылка для обновления
     * @return true если успешно инкрементнуто, false если превышен лимит
     */
    @Transactional
    protected boolean incrementUsesCount(CourseInviteLinkEntity inviteLink) {
        int newUsesCount = inviteLink.getUsesCount() + 1;

        if (inviteLink.getMaxUses() != null && newUsesCount > inviteLink.getMaxUses()) {
            return false;
        }

        inviteLink.setUsesCount(newUsesCount);
        inviteLink.setActive(inviteLink.getMaxUses() == null ||
                newUsesCount < inviteLink.getMaxUses());

        courseInviteLinkRepository.save(inviteLink);
        return true;
    }
}
