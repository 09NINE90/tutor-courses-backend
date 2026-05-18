package ru.razumoff.minio;

import io.minio.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.razumoff.exceptions.ErrorCode;
import ru.razumoff.exceptions.PlatformException;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import static ru.razumoff.Constants.Minio.PUBLIC_READ_POLICY_TEMPLATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileService implements IMinioFileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public String uploadCourseImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) return null;
        validateImage(imageFile);
        try {
            return uploadImage(imageFile, bucketName);
        } catch (Exception e) {
            log.error("Failed to upload image to bucket {}", bucketName, e);
            throw new PlatformException(ErrorCode.FAILED_UPLOAD_IMAGE);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);
            String path = uri.getPath().substring(1);
            String[] parts = path.split("/", 2);
            String bucket = parts[0];
            String object = parts[1];

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to delete image from bucket {}", bucketName, e);
            throw new PlatformException(ErrorCode.FAILED_DELETE_IMAGE);
        }
    }

    /**
     * Генерация публичной presigned URL (7 дней)
     */
    @Override
    public String generatePublicUrl(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) return "";
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(bucketName)
                            .object(s3Key)
                            .expiry(7 * 24 * 60 * 60)
                            .build()
            );
        } catch (Exception e) {
            log.error("Presigned failed for {}", s3Key, e);
            throw new RuntimeException("S3 access error", e);
        }
    }

    private String uploadImage(MultipartFile imageFile, String bucketName) throws Exception {
        String fileName = UUID.randomUUID() + "." + extractExtension(imageFile.getOriginalFilename());
        createBucketWithPolicy(bucketName);

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName).object(fileName)
                .stream(imageFile.getInputStream(), imageFile.getSize(), -1L)
                .contentType(imageFile.getContentType())
                .build());

        log.info("Success uploaded file {} to bucket {}", fileName, bucketName);
        return fileName;
    }

    public void createBucketWithPolicy(String bucketName) throws Exception {
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());

        if (bucketExists) {
            return;
        }

        MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                .bucket(bucketName).build();
        minioClient.makeBucket(makeBucketArgs);

        String policyJson = PUBLIC_READ_POLICY_TEMPLATE.formatted(bucketName);

        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                .bucket(bucketName)
                .config(policyJson)
                .build());
    }

    private void validateImage(@NotNull MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024 || !Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            log.error("Invalid image file: {}", file.getOriginalFilename());
            throw new PlatformException(ErrorCode.INVALID_IMAGE);
        }
    }

    private String extractExtension(String filename) {
        return filename != null ? filename.substring(filename.lastIndexOf('.') + 1) : "png";
    }
}