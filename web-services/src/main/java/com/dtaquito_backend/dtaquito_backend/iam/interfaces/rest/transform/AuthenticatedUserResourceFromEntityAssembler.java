package com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.transform;


import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        return new AuthenticatedUserResource(user.getId(), user.getName(), token);
    }
}
