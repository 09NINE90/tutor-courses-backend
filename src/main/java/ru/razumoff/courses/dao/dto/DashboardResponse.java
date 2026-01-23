package ru.razumoff.courses.dao.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Дашборд курсов")
public class DashboardResponse {

    @Schema(
            description = "Флаг - учитель или нет",
            example = "true"
    )
    @JsonProperty(value = "isTutor")
    private boolean isTutor;

    @ArraySchema(schema = @Schema(
            implementation = CourseRsDto.class
    ))
    private List<CourseRsDto> courses;
}
