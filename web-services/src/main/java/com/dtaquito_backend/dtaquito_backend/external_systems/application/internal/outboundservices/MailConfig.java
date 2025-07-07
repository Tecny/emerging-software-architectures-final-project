package com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.outboundservices;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Objects;
import java.util.Properties;

@Configuration
public class MailConfig {

    private static final Dotenv dotenv = Dotenv.load();

    @Bean
    public JavaMailSender javaMailSender() {
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

            mailSender.setHost(dotenv.get("SPRING_MAIL_HOST"));
            mailSender.setPort(Integer.parseInt(Objects.requireNonNull(dotenv.get("SPRING_MAIL_PORT"))));
            mailSender.setUsername(dotenv.get("SPRING_MAIL_USERNAME"));
            mailSender.setPassword(dotenv.get("SPRING_MAIL_PASSWORD"));

            Properties properties = mailSender.getJavaMailProperties();
            properties.put("mail.smtp.auth", dotenv.get("SPRING_MAIL_PROPERTIES_SMTP_AUTH"));
            properties.put("mail.smtp.starttls.enable", dotenv.get("SPRING_MAIL_PROPERTIES_SMTP_STARTTLS_ENABLE"));
            properties.put("mail.smtp.starttls.required", dotenv.get("SPRING_MAIL_PROPERTIES_SMTP_STARTTLS_REQUIRED"));

            return mailSender;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error configurando JavaMailSender", e);
        }
    }
}
