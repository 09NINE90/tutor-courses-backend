package ru.razumoff.courses.dao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.razumoff.courses.dao.enumz.EnrollmentStatus;

import java.util.UUID;

@Data
@Builder
@Schema(description = "Краткая информация об участнике курса")
public class CourseMemberRsDto {

    @Schema(
            description = "Уникальный идентификатор пользователя",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID userId;

    @Schema(
            description = "Флаг - профиль того, кто запросил или нет",
            example = "false"
    )
    @JsonProperty(value = "isMe")
    private boolean isMe;

    @Schema(
            description = "Email пользователя",
            example = "student@example.com"
    )
    private String email;

    @Schema(
            description = "Имя пользователя",
            example = "Иван"
    )
    private String firstName;

    @Schema(
            description = "Фамилия пользователя",
            example = "Иванов"
    )
    private String lastName;

    @Schema(
            description = "URL аватара пользователя (может быть null)",
            example = "https://example.com/avatars/123.jpg"
    )
    private String avatarUrl;

    @Schema(
            description = "Статус зачисления в курс",
            example = "ACTIVE",
            enumAsRef = true
    )
    private EnrollmentStatus enrollmentStatus;

    @Schema(
            description = "Человекочитаемое название статуса",
            example = "Активный"
    )
    private String statusLabel;
}
