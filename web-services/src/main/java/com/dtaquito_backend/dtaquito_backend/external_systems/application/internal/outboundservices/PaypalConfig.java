package com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.outboundservices;

import com.paypal.base.rest.APIContext;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaypalConfig {

    private static final Dotenv dotenv = Dotenv.configure().load();

    private String clientId = dotenv.get("PAYPAL_CLIENT_ID");
    private String clientSecret = dotenv.get("PAYPAL_CLIENT_SECRET");
    private String mode = dotenv.get("PAYPAL_MODE");

    @Bean
    public APIContext apiContext() {
        return new APIContext(clientId, clientSecret, mode);
    }
}
