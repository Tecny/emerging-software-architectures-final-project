package com.dtaquito_backend.dtaquito_backend.iam.infrastructure.hashing.bcrypt;


import com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * This interface is a marker interface for the BCrypt hashing service.
 * It extends the {@link HashingService} and {@link PasswordEncoder} interfaces.
 */
public interface BCryptHashingService extends HashingService, PasswordEncoder {
}