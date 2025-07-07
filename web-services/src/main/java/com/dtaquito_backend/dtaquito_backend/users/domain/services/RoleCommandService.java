package com.dtaquito_backend.dtaquito_backend.users.domain.services;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.commands.SeedRoleTypeCommand;

public interface RoleCommandService {

    void handle(SeedRoleTypeCommand command);
}