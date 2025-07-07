package com.dtaquito_backend.dtaquito_backend.iam.infrastructure.tokens.jwt;

import com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.tokens.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

/**
 * This interface is a marker interface for the JWT token service.
 * It extends the {@link TokenService} interface.
 * This interface is used to inject the JWT token service in the {} class.
 */
public interface BearerTokenService extends TokenService {

    /**
     * This method is responsible for extracting the JWT token from the HTTP request.
     * @param token the HTTP request
     * @return String the JWT token
     */
    String getBearerTokenFrom(HttpServletRequest token);
    String generateToken(String username, String userId, String role);
}
