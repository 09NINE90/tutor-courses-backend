package ru.razumoff.minio;

import org.springframework.web.multipart.MultipartFile;

public interface IMinioFileService {

    String uploadCourseImage(MultipartFile imageFile);

    String generatePublicUrl(String s3Key);

    void deleteImage(String s3Key);
}
