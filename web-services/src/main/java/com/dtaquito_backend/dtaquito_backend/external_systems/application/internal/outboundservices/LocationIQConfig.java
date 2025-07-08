package com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.outboundservices;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LocationIQConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
