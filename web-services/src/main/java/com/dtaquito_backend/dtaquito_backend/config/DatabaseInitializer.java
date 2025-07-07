package com.dtaquito_backend.dtaquito_backend.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatabaseInitializer {

    @Bean
    public ApplicationRunner initializeDatabase(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        return args -> {
            String checkUserQuery = "SELECT COUNT(*) FROM users WHERE email = 'admin@dtaquito.com'";
            Integer userCount = jdbcTemplate.queryForObject(checkUserQuery, Integer.class);

            if (userCount == 0) {
                String encryptedPassword = passwordEncoder.encode("DTaquito2025@");

                jdbcTemplate.update(
                        "INSERT INTO users (id, created_at, updated_at, credits, email, name, password, role_id) " +
                                "VALUES (?, NOW(), NOW(), ?, ?, ?, ?, ?)",
                        1, 0, "admin@dtaquito.com", "admin", encryptedPassword, 3
                );
            }

            String checkSubscriptionQuery = "SELECT COUNT(*) FROM subscriptions WHERE user_id = 1";
            Integer subscriptionCount = jdbcTemplate.queryForObject(checkSubscriptionQuery, Integer.class);

            if (subscriptionCount == 0) {
                jdbcTemplate.execute("INSERT INTO subscriptions (created_at, updated_at, allowed_sport_spaces, plan_id, user_id) " +
                        "VALUES (NOW(), NOW(), 0, 1, 1)");
            }
        };
    }
}
