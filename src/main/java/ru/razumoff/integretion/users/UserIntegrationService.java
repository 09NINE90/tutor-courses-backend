package ru.razumoff.integretion.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.razumoff.commonlib.dto.integration.ProfileRsDto;
import ru.razumoff.commonlib.exceptions.ErrorCode;
import ru.razumoff.commonlib.exceptions.PlatformException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserIntegrationService implements IUserIntegrationService {

    private final RestTemplate restTemplate;

    @Value("${integration.user.host}")
    private String baseUrl;

    @Value("${integration.user.get-user-profiles}")
    private String userProfilesApi;

    private static final String LOG_PREFIX = "Интеграция с сервисом пользователей";

    @Override
    public List<ProfileRsDto> getUserProfiles(List<UUID> userIds) {
        String fullUrl = String.format("%s%s",
                baseUrl,
                userProfilesApi
        );

        log.info("{}: Запрос на получение профилей. userIds: {}, path: {}", LOG_PREFIX, userIds, fullUrl);

        try {
            HttpEntity<List<UUID>> request = new HttpEntity<>(userIds);
            ResponseEntity<List<ProfileRsDto>> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<List<ProfileRsDto>>() {
                    }
            );

            log.info("{}: Получены профили пользователей: {}", LOG_PREFIX, response);
            return response.getBody();

        } catch (Exception ex) {
            log.error("{}: Ошибка при запросе профилей: {}", LOG_PREFIX, ex.getMessage(), ex);
            throw new PlatformException(ErrorCode.INTEGRATION_AUTH_SERVICE_ERROR);
        }
    }
}