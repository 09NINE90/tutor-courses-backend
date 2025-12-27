package ru.razumoff.courses.dao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@Schema(description = "Запрос для создания нового курса с базовой информацией")
public class CreateCourseRqDto {

    @Schema(
            description = "Название курса (обязательное поле)",
            example = "Алгоритмы и структуры данных на Java",
            minLength = 3,
            maxLength = 255
    )
    private String title;

    @Schema(
            description = "Описание курса (не более 2000 символов)",
            example = "Глубокое изучение алгоритмов: сортировки, графы, динамическое программирование и оптимизация."
    )
    private String description;

    @Schema(
            description = "URL изображения/обложки курса (опционально)",
            example = "https://example.com/images/algorithms-course.jpg"
    )
    @URL(message = "Неверный формат URL изображения курса.")
    private String imageUrl;
}
