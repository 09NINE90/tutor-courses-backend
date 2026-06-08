package ru.razumoff.courses.dao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseUpdateRequest {

    @Schema(description = "Название курса", example = "Android development learning")
    private String title;

    @Schema(description = "Описание курса", example = "Some description")
    private String description;

    @Schema(description = "Флаг удаления изображения курса", example = "true")
    private Boolean deleteImage;

}
