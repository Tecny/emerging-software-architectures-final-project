package com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;

public record SignUpCommand(String name, String email, String password, Role role) {
}
