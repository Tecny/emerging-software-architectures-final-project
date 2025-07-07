package com.dtaquito_backend.dtaquito_backend.users.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.hashing.HashingService;
import com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignInCommand;
import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignUpCommand;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.events.UserCreatedEvent;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.valueObjects.RoleTypes;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserCommandService;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserQueryService;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.RoleRepository;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TokenService tokenService;
    private final UserQueryService userQueryService;

    public UserCommandServiceImpl(UserRepository userRepository, RoleRepository roleRepository, ApplicationEventPublisher applicationEventPublisher,
                                  HashingService hashingService, TokenService tokenService,
                                  UserQueryService userQueryService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.userQueryService = userQueryService;
    }

    @Override
    public Optional<User> updatePassword(Long userId, String newPassword) {
        var user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String encryptedPassword = hashingService.encode(newPassword);
        user.setPassword(encryptedPassword);
        var updatedUser = userRepository.save(user);
        return Optional.of(updatedUser);
    }

    @Override
    public Optional<User> updateName(Long userId, String newName) {
        var user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setName(newName);
        var updatedUser = userRepository.save(user);
        return Optional.of(updatedUser);
    }

    @Override
    public Optional<User> updateEmail(Long userId, String newEmail) {
        var user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEmail(newEmail);
        var updatedUser = userRepository.save(user);
        return Optional.of(updatedUser);
    }

    @Override
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        System.out.println("UserCreatedEvent received for user ID: " + event.getUserId());
    }

    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (command.role() == null) {
            throw new IllegalArgumentException("Role name must not be null or empty");
        }
        var roleType = command.role();
        Optional<Role> role = roleRepository.findByRoleType(roleType.getRoleType());
        if (role.isEmpty()) {
            throw new IllegalArgumentException("Role not found: " + roleType);
        }

        if (command.email() == null || command.email().isEmpty()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }

        if (command.password() == null || command.password().isEmpty()) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }

        var user = new User(command.name(), command.email(), hashingService.encode(command.password()), role.get(), BigDecimal.valueOf(0));
        var createdUser = userRepository.save(user);

        UserCreatedEvent event = new UserCreatedEvent(this, createdUser.getId());
        applicationEventPublisher.publishEvent(event);

        return Optional.of(createdUser);
    }

//    @Override
//    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
//
//        var user = userRepository.findByEmail(command.email()).orElseThrow(() -> new IllegalArgumentException("User not found"));
//        RoleTypes userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(user.getId()));
//        if (!hashingService.matches(command.password(), user.getPassword()))
//            throw new RuntimeException("Invalid password");
//        var token = tokenService.generateToken(user.getEmail(), String.valueOf(user.getId()));
//        return Optional.of(ImmutablePair.of(user, token));
//    }

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {

        var user = userRepository.findByEmail(command.email()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        RoleTypes userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(user.getId()));

        if (userRole == RoleTypes.ADMIN) {
            if (!command.password().equals(user.getPassword())) {
                throw new RuntimeException("Invalid password");
            }
        } else {
            if (!hashingService.matches(command.password(), user.getPassword())) {
                throw new RuntimeException("Invalid password");
            }
        }

        var token = tokenService.generateToken(user.getEmail(), String.valueOf(user.getId()), user.getRole().getRoleType().name());
        return Optional.of(ImmutablePair.of(user, token));
    }

}