package ru.razumoff.integretion.config;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.razumoff.config.security.JwtUserPrincipal;

import java.io.IOException;

@Slf4j
@Component
public class JwtRequestInterceptor implements ClientHttpRequestInterceptor {

    /**
     * Перехватчик: проброс JWT токена в SecurityContext для внешних сервисов
     */
    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request, byte @NonNull [] body,
                                                 @NonNull ClientHttpRequestExecution execution) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtUserPrincipal principal) {
            String token = principal.getToken();
            if (token != null) {
                request.getHeaders().setBearerAuth(token);
                log.debug("🔄 JWT forwarded: {} -> {}", request.getMethod(), request.getURI());
                return execution.execute(request, body);
            }
        }

        log.warn("⚠️ No JWT token in SecurityContext for: {}", request.getURI());
        return execution.execute(request, body);
    }

}
