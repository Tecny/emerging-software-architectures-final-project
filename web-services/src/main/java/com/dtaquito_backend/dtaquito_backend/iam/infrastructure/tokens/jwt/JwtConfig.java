package com.dtaquito_backend.dtaquito_backend.iam.infrastructure.tokens.jwt;

import io.jsonwebtoken.security.Keys;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {
    private static final Dotenv dotenv = Dotenv.configure().load();

    private String secret = dotenv.get("JWT_SECRET");

    @Bean
    public SecretKey signingKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}