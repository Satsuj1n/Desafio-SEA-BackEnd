package com.felipelima.clientmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class that creates a RestTemplate bean.
 * 
 * RestTemplate is Spring's synchronous HTTP client (like Python's requests library).
 * By creating it as a @Bean, it becomes injectable via constructor injection
 * anywhere in the application.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
