package com.dtaquito_backend.dtaquito_backend.reservations.domain.model.entities;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class QRCodeGenerator {

    private final Key key;
    private final Set<String> usedTokenCache = new HashSet<>();

    public QRCodeGenerator() {
        Dotenv dotenv = Dotenv.load();
        String secret = dotenv.get("JWT_SECRET");
        assert secret != null;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateQrToken(Long reservationId, Long userId, String startTimeStr, String endTimeStr, String gameDayStr) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalTime startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"));

        LocalDate gameDay = LocalDate.parse(gameDayStr, dateFormatter);

        ZoneId limaZone = ZoneId.of("America/Lima");
        ZonedDateTime limaNow = ZonedDateTime.now(limaZone);

        if (!limaNow.toLocalDate().equals(gameDay)) {
            throw new RuntimeException("La fecha actual no coincide con el día del juego.");
        }

        LocalDateTime startDateTime = LocalDateTime.of(gameDay, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(gameDay, endTime);

        LocalDateTime validFromDateTime = startDateTime.minusHours(1);

        if (limaNow.isBefore(validFromDateTime.atZone(limaZone)) || limaNow.isAfter(endDateTime.atZone(limaZone))) {
            throw new RuntimeException("El token QR no es válido fuera del rango de la reserva.");
        }

        Date expirationDate = Date.from(endDateTime.atZone(limaZone).toInstant());

        return Jwts.builder()
                .claim("reservationId", reservationId)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateQrToken(String token) {
        return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public boolean checkIfTokenUsed(String token) {
        return usedTokenCache.contains(token);
    }

    public void markTokenAsUsed(String token) {
        usedTokenCache.add(token);
    }
}