package ru.razumoff.courses.service;

import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.courses.dao.dto.*;
import ru.razumoff.courses.dao.dto.internal.InviteUserDto;
import ru.razumoff.jwt.JwtUserPrincipal;

import java.util.List;
import java.util.UUID;

public interface ICourseService {

    DashboardResponse getCoursesDashboard(JwtUserPrincipal principal, int pageNumber, int pageSize, String sortBy);

    void createCourse(JwtUserPrincipal principal, CreateCourseRqDto request, MultipartFile image);

    CoursePageRsDto getCourseById(JwtUserPrincipal principal, UUID courseId);

    List<CourseMemberRsDto> getCourseMembersById(JwtUserPrincipal principal, UUID courseId);

    void inviteUserToCourse(InviteUserDto request);

    void confirmInvite(JwtUserPrincipal principal, UUID courseId);

    InviteLinkRsDto createInviteLink(JwtUserPrincipal principal, CreateInviteLinkRqDto request, UUID courseId);

    InviteLinkRsDto getInviteLinks(JwtUserPrincipal principal, UUID courseId);

    void joinWithInvite(UUID courseId, JoinCourseRqDto request, JwtUserPrincipal principal);

    void viewCourse(JwtUserPrincipal principal, UUID courseId);
}
