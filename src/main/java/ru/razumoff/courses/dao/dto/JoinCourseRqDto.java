package ru.razumoff.courses.dao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinCourseRqDto {

    @Schema(description = "UUID пригласительной ссылки",
            example = "8bce9197-ec92-4d23-8040-7384c17e19a2")
    private UUID token;

}
