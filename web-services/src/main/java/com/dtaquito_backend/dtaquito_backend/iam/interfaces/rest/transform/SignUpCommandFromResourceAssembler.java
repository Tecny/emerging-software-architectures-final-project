package com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignUpCommand;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources.SignUpResource;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var role = resource.role() != null ? Role.fromNameToRole(resource.role()) : null;
        return new SignUpCommand(resource.name(), resource.email(), resource.password(), role);
    }
}