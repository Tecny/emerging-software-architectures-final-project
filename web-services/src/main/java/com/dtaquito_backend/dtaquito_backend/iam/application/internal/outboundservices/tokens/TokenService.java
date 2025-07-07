package com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.tokens;

/**
 * TokenService interface
 * This interface is used to generate and validate tokens
 */
public interface TokenService {

    /**
     * Generate a token for a given username
     * @return String the token
     */
    String generateToken(String email, String userId, String role);

    /**
     * Extract the username from a token
     * @param token the token
     * @return String the username
     */
    String getEmailFromToken(String token);

    String getUserIdFromToken(String token);
    /**
     * Validate a token
     * @param token the token
     * @return boolean true if the token is valid, false otherwise
     */
    boolean validateToken(String token);


}
