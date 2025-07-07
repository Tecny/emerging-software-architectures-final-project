package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.SeedSportTypeCommand;

public interface SportCommandService {
    void handle(SeedSportTypeCommand command);
}
