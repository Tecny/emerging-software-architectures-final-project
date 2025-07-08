package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.CreateSportSpacesCommand;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.events.SportSpacesCreatedEvent;

import java.io.IOException;
import java.util.Optional;

public interface SportSpacesCommandService {

    Optional<SportSpaces> handle(Long id, CreateSportSpacesCommand command) throws IOException;
    Optional<SportSpaces> handleUpdate(Long id, CreateSportSpacesCommand command) throws IOException;
    void handleSportSpacesCreatedEvent(SportSpacesCreatedEvent event);
    byte[] getImageById(Long id);
}