package ru.razumoff.courses.service;

import ru.razumoff.courses.dao.dto.CreateInviteLinkRqDto;
import ru.razumoff.courses.dao.dto.InviteLinkRsDto;
import ru.razumoff.courses.dao.dto.JoinCourseRqDto;
import ru.razumoff.jwt.JwtUserPrincipal;

import java.util.UUID;

public interface ICourseInviteLinkService {

    InviteLinkRsDto createInviteLink(JwtUserPrincipal principal, CreateInviteLinkRqDto request, UUID courseId);

    InviteLinkRsDto getInviteLinks(JwtUserPrincipal principal, UUID courseId);

    void joinWithInvite(UUID courseId, JoinCourseRqDto request, JwtUserPrincipal principal);

}
