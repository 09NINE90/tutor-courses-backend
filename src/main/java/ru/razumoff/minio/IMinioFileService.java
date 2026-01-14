package ru.razumoff.minio;

import org.springframework.web.multipart.MultipartFile;

public interface IMinioFileService {

    String uploadCourseImage(MultipartFile imageFile);

    void deleteImage(String oldAvatarUrl);
}
