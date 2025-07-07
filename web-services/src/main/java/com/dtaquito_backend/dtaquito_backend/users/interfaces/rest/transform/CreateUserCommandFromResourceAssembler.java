package com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.commands.CreateUserCommand;
import com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.resources.CreateUserResource;

public class CreateUserCommandFromResourceAssembler {

    public static CreateUserCommand toCommandFromResource(CreateUserResource resource) {
        return new CreateUserCommand(resource.name(), resource.email(), resource.password(), resource.roleId());
    }
}