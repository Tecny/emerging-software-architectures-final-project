package com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.transform;


import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignInCommand;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource signInResource) {
        return new SignInCommand(signInResource.email(), signInResource.password());
    }
}
