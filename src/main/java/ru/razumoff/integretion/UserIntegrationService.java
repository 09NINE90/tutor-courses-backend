package ru.razumoff.integretion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.razumoff.commonlib.exceptions.ErrorCode;
import ru.razumoff.commonlib.exceptions.PlatformException;
import ru.razumoff.courses.dao.dto.ProfileRsDto;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserIntegrationService implements IUserIntegrationService {

    private final RestTemplate restTemplate;

    @Value("${integration.user.host}")
    private String authServiceBaseUrl;

    @Value("${integration.user.user-profile-api}")
    private String userProfileApi;

    private static final String LOG_PREFIX = "Интеграция с сервисом пользователей";

    @Override
    public ProfileRsDto getUserProfile(UUID userId) {
        String url = String.format(
                "%s%s%s",
                authServiceBaseUrl,
                userProfileApi,
                userId
        );
        log.info("{}: Запрос на получение данных пользователя. path: {}", LOG_PREFIX, url);
        try {
            var response = restTemplate.getForObject(url, ProfileRsDto.class);
            if (response == null) {
                log.warn("Пустой ответ от сервиса пользователей");
                throw new PlatformException(ErrorCode.AUTH_USER_NOT_FOUND);
            }
            log.info("{}: Успешный ответ от сервиса пользователей: {}", LOG_PREFIX, response);
            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new PlatformException(ErrorCode.INTEGRATION_AUTH_SERVICE_ERROR);
        }
    }
}