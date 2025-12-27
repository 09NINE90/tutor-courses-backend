package ru.razumoff.courses.dao.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ProfileRsDto {

    private UUID id;
    private String email;
    private String[] roles;
}
