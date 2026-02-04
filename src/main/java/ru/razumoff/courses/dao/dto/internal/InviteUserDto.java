package ru.razumoff.courses.dao.dto.internal;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class InviteUserDto {

    @NotNull
    private UUID ownerId;

    @NotNull
    private UUID userId;

    @NotNull
    private UUID courseId;
}
