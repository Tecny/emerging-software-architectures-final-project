package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.SeedGameTypeCommand;

public interface GameCommandService {

    void handle(SeedGameTypeCommand command);
}
