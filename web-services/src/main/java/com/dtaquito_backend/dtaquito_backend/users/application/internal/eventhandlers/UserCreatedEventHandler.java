package com.dtaquito_backend.dtaquito_backend.users.application.internal.eventhandlers;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.events.UserCreatedEvent;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.queries.GetUserByIdQuery;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserCommandService;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserQueryService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class UserCreatedEventHandler {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    public UserCreatedEventHandler(UserQueryService userQueryService, UserCommandService userCommandService) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
    }

    @EventListener(UserCreatedEvent.class)
    public void on(UserCreatedEvent event) {
        System.out.println("UserCreatedEvent received for user ID: " + event.getUserId());

        userCommandService.handleUserCreatedEvent(event);

        var getUserByIdQuery = new GetUserByIdQuery(event.getUserId());

        var user = userQueryService.handle(getUserByIdQuery);

        if (user.isPresent()) {
            System.out.println("User with ID " + event.getUserId() + " has been created.");
        } else {
            System.out.println("No user found with ID " + event.getUserId());
        }
    }
}