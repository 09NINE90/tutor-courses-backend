package ru.razumoff.courses.dao.dto.internal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InviteUserDto {

    @NotNull
    private UUID ownerId;

    @NotNull
    private UUID userId;

    @NotNull
    private UUID courseId;
}
