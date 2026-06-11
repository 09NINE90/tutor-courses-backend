package ru.razumoff.courses.service;

import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.courses.dao.dto.*;
import ru.razumoff.jwt.JwtUserPrincipal;

import java.util.UUID;

public interface ICourseService {

    void createCourse(JwtUserPrincipal principal, CreateCourseRqDto request, MultipartFile image);

    CoursePageRsDto getCourseById(JwtUserPrincipal principal, UUID courseId);

    void viewCourse(JwtUserPrincipal principal, UUID courseId);

    CourseRsDto updateCourse(UUID courseId, CourseUpdateRequest request, JwtUserPrincipal principal);

    CourseRsDto updateCourseImage(UUID courseId, MultipartFile image, JwtUserPrincipal principal);
}
