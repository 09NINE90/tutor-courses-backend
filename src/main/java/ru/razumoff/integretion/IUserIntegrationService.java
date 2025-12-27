package ru.razumoff.integretion;

import ru.razumoff.courses.dao.dto.ProfileRsDto;

import java.util.UUID;

public interface IUserIntegrationService {

    ProfileRsDto getUserProfile(UUID userId);

}
