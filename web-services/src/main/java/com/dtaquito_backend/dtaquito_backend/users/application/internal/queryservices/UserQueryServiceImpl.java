package com.dtaquito_backend.dtaquito_backend.users.application.internal.queryservices;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.queries.GetUserByIdQuery;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserQueryService;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public String getUserRoleByUserId(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(u -> u.getRole().getRoleType().name()).orElse(null);
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        Long userId = query.id();
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new NoSuchElementException("User with ID " + userId + " not found");
        }
        return user;
    }
}