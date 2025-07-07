package com.dtaquito_backend.dtaquito_backend.users.interfaces.rest;

import java.util.*;
import java.util.regex.Pattern;

import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources.SignUpResource;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_backend.dtaquito_backend.subscriptions.infrastructure.persistance.jpa.PlanRepository;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.valueObjects.RoleTypes;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.resources.UpdateEmailUserResource;
import com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.resources.UpdateNameUserResource;
import com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.resources.UpdatePasswordUserResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.services.SubscriptionsCommandService;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.commands.CreateSubscriptionsCommand;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserCommandService;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserQueryService;
import com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.resources.UserResource;
import com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.transform.UserResourceFromEntityAssembler;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Users Controller")
@Slf4j
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final SubscriptionsCommandService subscriptionsCommandService;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserCommandService userCommandService, UserQueryService userQueryService, SubscriptionsCommandService subscriptionsCommandService, PlanRepository planRepository,
                          UserRepository userRepository) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.subscriptionsCommandService = subscriptionsCommandService;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> createUser(@RequestBody SignUpResource resource, HttpServletRequest request) {
        try {
            if (resource.role() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN).body("Roles cannot be null or empty");
            }
            if (!EnumUtils.isValidEnum(RoleTypes.class, resource.role())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN).body("Invalid role: " + resource.role());
            }
            if (resource.role().equalsIgnoreCase("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("Cannot register as ADMIN");
            }

            String password = resource.password();

            String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{16,}$";
            Pattern pattern = Pattern.compile(passwordRegex);

            if (!pattern.matcher(password).matches()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("Password must be at least 16 characters long, and contain at least one uppercase letter, one lowercase letter, one digit, and one special character.");
            }

            Optional<User> existingUser = userRepository.findByEmail(resource.email());
            if (existingUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN).body("User with this email already exists");
            }

            var user = userCommandService.handle(SignUpCommandFromResourceAssembler.toCommandFromResource(resource));
            if (user.isPresent()) {
                userRepository.save(user.get());
                request.getSession().setAttribute("userId", user.get().getId().toString());

                PlanTypes planType = PlanTypes.FREE;
                Plan selectedPlan = planRepository.findByPlanType(planType)
                        .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
                CreateSubscriptionsCommand command = new CreateSubscriptionsCommand(selectedPlan.getId(), user.get().getId(), null);
                subscriptionsCommandService.handle(command);

                log.info("Created subscription for user: {} with plan: {}", user.get().getId(), planType.name().toLowerCase());

                return ResponseEntity.ok(Map.of("message", "User registered successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN).body("User registration failed");
            }
        } catch (Exception e) {
            log.error("Error creating user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body("Error creating user");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResource>> getAllUsers(Authentication authentication) {

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();
        RoleTypes userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(userId));;
        if (userRole == RoleTypes.ADMIN) {
            var users = userQueryService.getAllUsers();
            var userResources = users.stream().map(UserResourceFromEntityAssembler::toResourceFromEntity).toList();
            return ResponseEntity.ok(userResources);
        }else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @PutMapping("/name")
    public ResponseEntity<Map<String, String>> updateName(@RequestBody UpdateNameUserResource request, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        String newName = request.getName();
        Optional<User> user = userCommandService.updateName(userId, newName);

        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Name updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }

    @PutMapping("/email")
    public ResponseEntity<Map<String, String>> updateEmail(@RequestBody UpdateEmailUserResource request, Authentication authentication, HttpServletRequest servletRequest) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        String newEmail = request.getNewEmail();
        Optional<User> user = userCommandService.updateEmail(userId, newEmail);

        if (user.isPresent()) {
            servletRequest.getSession().invalidate();
            return ResponseEntity.ok(Map.of("message", "Email updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestBody UpdatePasswordUserResource request, Authentication authentication, HttpServletRequest servletRequest) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        String newPassword = request.getNewPassword();

        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{16,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        if (!pattern.matcher(newPassword).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Password must be at least 16 characters long, and contain at least one uppercase letter, one lowercase letter, one digit, and one special character."));
        }

        Optional<User> user = userCommandService.updatePassword(userId, newPassword);

        if (user.isPresent()) {
            servletRequest.getSession().invalidate();
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResource> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        System.out.println("🔍 Tipo de Principal: " + principal.getClass().getName());


        return userRepository.findByEmail(principal.getUsername())
                .map(user -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}