package ru.razumoff.courses.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.commonlib.exceptions.ErrorCode;
import ru.razumoff.commonlib.exceptions.PlatformException;
import ru.razumoff.config.security.JwtUserPrincipal;
import ru.razumoff.courses.dao.dto.CourseRsDto;
import ru.razumoff.courses.dao.dto.CreateCourseRqDto;
import ru.razumoff.courses.dao.entity.CourseEntity;
import ru.razumoff.courses.dao.repository.CourseRepository;
import ru.razumoff.minio.IMinioFileService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {

    private final CourseRepository repository;
    private final IMinioFileService minioService;

    @Override
    public List<CourseRsDto> getAllCoursesByUser(JwtUserPrincipal principal) {
        UUID userId = principal.getId();
        List<CourseEntity> entities = repository.findAllByOwnerIdOrderByCreatedAtDesc(userId);

        if (entities.isEmpty()) return Collections.emptyList();

        List<CourseRsDto> result = new ArrayList<>();
        for (CourseEntity entity : entities) {
            result.add(
                    CourseRsDto.builder()
                            .id(entity.getId())
                            .title(entity.getTitle())
                            .description(entity.getDescription())
                            .imageUrl(entity.getImageUrl())
                            .build()
            );
        }

        return result;
    }

    @Override
    public void createCourse(JwtUserPrincipal principal,
                             CreateCourseRqDto request, MultipartFile image) {
        UUID userId = principal.getId();

        String imageUrl = null;
        if (image != null && !image.isEmpty()){
            imageUrl = minioService.uploadCourseImage(image);
        }

        CourseEntity entity = new CourseEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setImageUrl(imageUrl);
        entity.setOwnerId(userId);

        repository.save(entity);
    }

    @Override
    public CourseRsDto getCourseById(JwtUserPrincipal principal, UUID courseId) {
        UUID userId = principal.getId();
        CourseEntity entity = repository.findByOwnerIdAndId(userId, courseId).orElseThrow(
                () -> new PlatformException(ErrorCode.COURSE_NOT_FOUND)
        );

        return CourseRsDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .build();
    }
}
