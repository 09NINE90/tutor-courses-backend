package ru.razumoff.integretion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(List<ClientHttpRequestInterceptor> interceptors) {
        RestTemplate rt = new RestTemplate();
        rt.setInterceptors(interceptors);
        return rt;
    }

}