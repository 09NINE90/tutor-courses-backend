package ru.razumoff.integretion.users;


import ru.razumoff.dto.integration.ProfileRsDto;

import java.util.List;
import java.util.UUID;

public interface IUserIntegrationService {

    List<ProfileRsDto> getUserProfiles(List<UUID> userIds);

}
