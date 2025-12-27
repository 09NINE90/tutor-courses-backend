package ru.razumoff.courses.service;

import ru.razumoff.config.security.JwtUserPrincipal;
import ru.razumoff.courses.dao.dto.CourseRsDto;
import ru.razumoff.courses.dao.dto.CreateCourseRqDto;

import java.util.List;
import java.util.UUID;

public interface ICourseService {
    List<CourseRsDto> getAllCoursesByUser(JwtUserPrincipal principal);

    void createCourse(JwtUserPrincipal principal, CreateCourseRqDto request);

    CourseRsDto getCourseById(JwtUserPrincipal principal, UUID courseId);
}
