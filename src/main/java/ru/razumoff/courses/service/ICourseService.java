package ru.razumoff.courses.service;

import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.config.security.JwtUserPrincipal;
import ru.razumoff.courses.dao.dto.CourseRsDto;
import ru.razumoff.courses.dao.dto.CreateCourseRqDto;
import ru.razumoff.courses.dao.dto.DashboardResponse;

import java.util.UUID;

public interface ICourseService {

    DashboardResponse getCoursesDashboard(JwtUserPrincipal principal);

    void createCourse(JwtUserPrincipal principal, CreateCourseRqDto request, MultipartFile image);

    CourseRsDto getCourseById(JwtUserPrincipal principal, UUID courseId);
}
