package com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.commandservices;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SecretKey secretKey;
    private final Map<String, Boolean> tokenStatusMap = new HashMap<>();
    private static final Dotenv dotenv = Dotenv.load();

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;

        String secret = dotenv.get("JWT_SECRET");
        String paddedSecret = String.format("%-64s", secret).substring(0, 64);
        this.secretKey = Keys.hmacShaKeyFor(paddedSecret.getBytes(StandardCharsets.UTF_8));
    }

    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Solicitud de restablecimiento de contraseña");
        message.setText("Para restablecer su contraseña, haga clic en el enlace a continuación:\n" +
                "http://localhost:4200/reset-password?token=" + token);
        mailSender.send(message);
    }

    public String generatePasswordResetToken(String email) {

        cleanExpiredTokens();
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 10))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        tokenStatusMap.put(token, false);

        return token;
    }

    public void cleanExpiredTokens() {
        tokenStatusMap.entrySet().removeIf(entry -> {
            String token = entry.getKey();
            try {
                Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token);
                return false;
            } catch (ExpiredJwtException e) {
                return true;
            } catch (JwtException e) {
                return true;
            }
        });
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean isTokenUsed(String token) {
        return tokenStatusMap.getOrDefault(token, true);
    }

    public void markTokenAsUsed(String token) {
        tokenStatusMap.put(token, true);
    }
}
