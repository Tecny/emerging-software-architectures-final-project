package com.dtaquito_backend.dtaquito_backend.iam.infrastructure.tokens.jwt.services;

import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.github.cdimascio.dotenv.Dotenv;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;

@Service
public class TokenServiceImpl implements BearerTokenService {
    private final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);
    private final SecretKey signingKey;
    private static final Dotenv dotenv = Dotenv.configure().load();

    public TokenServiceImpl(SecretKey signingKey) {
        this.signingKey = signingKey;
    }

    private int expirationDays = Integer.parseInt(Objects.requireNonNull(dotenv.get("JWT_EXPIRATION_DAYS")));

    @Override
    public String generateToken(String username, String userId, String role) {
        LOGGER.debug("Generating token for username: {}", username);
        String token = buildTokenWithDefaultParameters(username, userId, role);
        LOGGER.debug("Generated token: {}", token);
        return token;
    }

    private String buildTokenWithDefaultParameters(String username, String userId, String role) {
        var issuedAt = new Date();
        var expiration = DateUtils.addDays(issuedAt, expirationDays);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", String.class);
    }

    @Override
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        LOGGER.debug("Validating token: {}", token);
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            LOGGER.info("Token is valid");
            return true;
        } catch (SignatureException e) {
            LOGGER.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOGGER.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Extraer el token sin "Bearer "
            LOGGER.info("Received JWT from Authorization header: {}", token);
            return token;
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    LOGGER.info("Received JWT from cookie: {}", token);
                    return token;
                }
            }
        }

        LOGGER.info("JWT not found in header or cookies");
        return null;
    }

}