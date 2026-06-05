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
public class CreateInviteLinkRqDto {

    @Schema(description = "Максимальное количество использований ссылки",
            example = "100",
            minimum = "1",
            nullable = true)
    private Integer maxUses;

    @Schema(description = "Срок действия ссылки в днях",
            example = "30",
            minimum = "1",
            maximum = "365")
    private Integer expiresDays;

}
