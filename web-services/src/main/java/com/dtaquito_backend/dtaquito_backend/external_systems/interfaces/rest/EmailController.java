package com.dtaquito_backend.dtaquito_backend.external_systems.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.commandservices.EmailService;
import com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.commandservices.PasswordResetTokenService;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.entities.ForgotPassword;
import com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.entities.ResetPassword;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/recover-password")
public class EmailController {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService passwordResetTokenService;

    public EmailController(EmailService emailService, UserRepository userRepository, @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder, PasswordResetTokenService passwordResetTokenService) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPassword request) {
        passwordResetTokenService.createPasswordResetTokenForUser(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPassword request) {

        String token = request.getToken();
        String newPassword = request.getNewPassword();

        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password cannot be empty"));
        }

        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{16,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        if (!pattern.matcher(newPassword).matches()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password must be at least 16 characters long, and contain at least one uppercase letter, one lowercase letter, one digit, and one special character."));
        }

        if (emailService.isTokenUsed(token)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token has already been used"));
        }

        String email;
        try {
            email = emailService.getEmailFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
        }

        User user = userOptional.get();
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        emailService.markTokenAsUsed(token);

        return ResponseEntity.ok(Map.of("message", "Password has been reset"));
    }
}