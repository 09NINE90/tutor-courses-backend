package ru.razumoff.courses.dao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@Schema(description = "Краткая информация о курсе с плагом роли")
public class CoursePageRsDto {

    Set<String> permissions;

    @Schema(
            description = "Статус участника в курсе",
            example = "INVITED"
    )
    private String status;

    @Schema(implementation = CourseRsDto.class)
    private CourseRsDto course;
}
