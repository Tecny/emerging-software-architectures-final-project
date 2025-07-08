package com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {

    public static UserResource toResourceFromEntity(User entity) {
        return new UserResource(entity.getId(), entity.getName(), entity.getEmail(), entity.getRole().getRoleType().name().toUpperCase(), entity.getCredits());
    }
}