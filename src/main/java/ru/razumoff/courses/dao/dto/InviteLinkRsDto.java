package ru.razumoff.courses.dao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "DTO пригласительной ссылки на курс")
public class InviteLinkRsDto {

    @Schema(description = "Уникальный токен ссылки",
            example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID token;

    @Schema(description = "Полная URL пригласительная ссылка",
            example = "https://app.ru/course/90fce4d0/invite/?token=f47ac10b")
    private String inviteLink;

    @Schema(description = "Дата истечения срока действия ссылки",
            example = "2026-04-04T12:00:00Z")
    private OffsetDateTime expiresAt;

    @Schema(description = "Количество использований ссылки",
            example = "5")
    private Integer usesCount;

    @Schema(description = "Максимальное количество использований (null = неограничено)",
            example = "25")
    private Integer maxUses;

}
