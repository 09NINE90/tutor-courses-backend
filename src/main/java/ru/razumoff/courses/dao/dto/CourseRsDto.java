package ru.razumoff.courses.dao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@Schema(description = "Краткая информация о курсе, используемая в списках и деталях курса")
public class CourseRsDto {

    @Schema(
            description = "Уникальный идентификатор курса",
            example = "c7c1f4c8-9f1d-4c3d-9b6b-0d4a9c123456"
    )
    private UUID id;

    @Schema(
            description = "Название курса",
            example = "Введение в Java для начинающих"
    )
    private String title;

    @Schema(
            description = "Краткое описание содержания курса",
            example = "Базовый курс по Java: синтаксис, ООП, коллекции, работа с файлами."
    )
    private String description;

    @Schema(
            description = "URL обложки/изображения курса",
            example = "https://example.com/images/java-course.png"
    )
    private String imageUrl;
}
