package ru.razumoff.courses.service;

import ru.razumoff.courses.dao.dto.CourseMemberRsDto;
import ru.razumoff.courses.dao.dto.internal.InviteUserDto;
import ru.razumoff.jwt.JwtUserPrincipal;

import java.util.List;
import java.util.UUID;

public interface ICourseMemberService {

    List<CourseMemberRsDto> getMembers(JwtUserPrincipal principal, UUID courseId);

    void inviteUser(InviteUserDto request);

    void confirmInvite(JwtUserPrincipal principal, UUID courseId);

}
