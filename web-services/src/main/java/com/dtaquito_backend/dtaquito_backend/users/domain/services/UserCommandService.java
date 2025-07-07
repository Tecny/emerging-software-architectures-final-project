package com.dtaquito_backend.dtaquito_backend.users.domain.services;

import java.util.Optional;

import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignInCommand;
import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignUpCommand;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.events.UserCreatedEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;

public interface UserCommandService {

    Optional<User> handle(SignUpCommand command);
    Optional<ImmutablePair<User, String>> handle(SignInCommand command);
    Optional<User> updatePassword(Long userId, String newPassword);
    Optional<User> updateName(Long userId, String newName);
    Optional<User> updateEmail(Long userId, String newEmail);
    void handleUserCreatedEvent(UserCreatedEvent event);
}
