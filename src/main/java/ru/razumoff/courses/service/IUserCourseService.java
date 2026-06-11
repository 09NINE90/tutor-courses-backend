package ru.razumoff.courses.service;

import ru.razumoff.courses.dao.dto.DashboardResponse;
import ru.razumoff.jwt.JwtUserPrincipal;

public interface IUserCourseService {

    DashboardResponse getCoursesDashboard(JwtUserPrincipal principal, int pageNumber, int pageSize, String sortBy);

}
